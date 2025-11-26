package com.home.service.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.UserService;
import com.home.service.dto.UserRegistrationRequest;
import com.home.service.models.ServiceCategory;
import com.home.service.models.Services;
import com.home.service.models.User;
import com.home.service.models.enums.UserRole;
import com.home.service.models.Business;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class CombinedExcelImportService {

    private final ServiceCategoryRepository categoryRepo;
    private final ServiceRepository servicesRepo;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CombinedExcelImportService(ServiceCategoryRepository categoryRepo,
                                      ServiceRepository servicesRepo,
                                      UserService userService,
                                      UserRepository userRepository,
                                      BusinessRepository businessRepository) {
        this.categoryRepo = categoryRepo;
        this.servicesRepo = servicesRepo;
        this.userService = userService;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    public static class CombinedImportResult {
        public int servicesCreated;
        public int companiesCreated;
        public int rowsProcessed;
        public List<String> messages = new ArrayList<>();
    }

    public CombinedImportResult importAll(MultipartFile file, Long defaultCategoryId, String commonPassword, Integer sheetIndex) {
        // defaultCategoryId is ignored per new requirements; we'll start from the first category
        if (commonPassword == null || commonPassword.isBlank()) {
            throw new IllegalArgumentException("commonPassword is required");
        }
        ServiceCategory category = categoryRepo.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ServiceCategory found"));

        CombinedImportResult result = new CombinedImportResult();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try {
            if (filename.endsWith(".csv")) {
                processCsv(file, category, commonPassword, result);
            } else {
                // Backward compatibility: single sheetIndex if provided, else 0
                processXlsxMulti(file, category, commonPassword, new int[] { sheetIndex != null ? sheetIndex : 0 }, result);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import: " + e.getMessage(), e);
        }
        return result;
    }

    // New overload: supports multiple sheet indices and process-all option
    public CombinedImportResult importAll(MultipartFile file, Long defaultCategoryId, String commonPassword,
                                          Integer sheetIndex, String sheetIndicesCsv, Boolean processAllSheets) {
        // defaultCategoryId is ignored per new requirements; we'll start from the first category
        if (commonPassword == null || commonPassword.isBlank()) {
            throw new IllegalArgumentException("commonPassword is required");
        }
        ServiceCategory category = categoryRepo.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ServiceCategory found"));

        CombinedImportResult result = new CombinedImportResult();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try {
            if (filename.endsWith(".csv")) {
                processCsv(file, category, commonPassword, result);
            } else {
                int[] indices;
                try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
                    int sheetCount = wb.getNumberOfSheets();
                    if (processAllSheets != null && processAllSheets) {
                        indices = new int[sheetCount];
                        for (int i = 0; i < sheetCount; i++) indices[i] = i;
                    } else if (sheetIndicesCsv != null && !sheetIndicesCsv.isBlank()) {
                        String[] parts = sheetIndicesCsv.split(",");
                        List<Integer> idxList = new ArrayList<>();
                        for (String p : parts) {
                            try {
                                int idx = Integer.parseInt(p.trim());
                                if (idx < 0 || idx >= sheetCount) {
                                    throw new IllegalArgumentException("Invalid sheet index: " + idx + ", total sheets: " + sheetCount);
                                }
                                idxList.add(idx);
                            } catch (NumberFormatException nfe) {
                                throw new IllegalArgumentException("Invalid sheet index value: '" + p + "'");
                            }
                        }
                        indices = idxList.stream().mapToInt(Integer::intValue).toArray();
                    } else {
                        int idx = sheetIndex != null ? sheetIndex : 0;
                        if (idx < 0 || idx >= sheetCount) {
                            throw new IllegalArgumentException("Invalid sheet index: " + idx + ", total sheets: " + sheetCount);
                        }
                        indices = new int[] { idx };
                    }
                }
                processXlsxMulti(file, category, commonPassword, indices, result);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import: " + e.getMessage(), e);
        }
        return result;
    }

    private void processXlsxMulti(MultipartFile file, ServiceCategory category, String commonPassword, int[] sheetIndices, CombinedImportResult result) throws Exception {
        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
            // Pre-compute ordered leaf services for the first category
            List<Services> leafOrder = computeLeafServicesOrdered(category);
            if (leafOrder.isEmpty()) {
                result.messages.add("No leaf services found for the first category; companies will not be linked.");
            }
            for (int sheetIdx : sheetIndices) {
                if (sheetIdx < 0 || sheetIdx >= wb.getNumberOfSheets()) {
                    throw new IllegalArgumentException("Invalid sheet index: " + sheetIdx);
                }
                Sheet sheet = wb.getSheetAt(sheetIdx);
                Iterator<Row> it = sheet.rowIterator();
                if (!it.hasNext()) continue; // empty sheet
                Row header = it.next();
                ColumnsMap cm = mapHeader(header);

                int leafIdx = -1; // current pointer into leaf services; starts before first
                boolean previousCategoryHasText = false;
                String lastNonEmptySubcategory = null; // used when category column absent
                boolean groupByCategory = cm.categoryIdx >= 0;
                while (it.hasNext()) {
                    Row row = it.next();
                    // Only SUBCATEGORY marker is used to advance leaf pointer
                    String categoryText = groupByCategory ? readByIndex(row, cm.categoryIdx) : "";
                    String subcategoryText = cm.subcategoryIdx >= 0 ? readByIndex(row, cm.subcategoryIdx) : "";
                    String companyName = readByIndex(row, cm.companyIdx);
                    String telephone = readByIndex(row, cm.telephoneIdx);
                    String mobile = readByIndex(row, cm.mobileIdx);
                    String whatsapp = readByIndex(row, cm.whatsappIdx);
                    String telegram = readByIndex(row, cm.telegramIdx);
                    String email = readByIndex(row, cm.emailIdx);
                    String website = readByIndex(row, cm.websiteIdx);
                    if (groupByCategory) {
                        boolean hasCategoryText = !isBlank(categoryText);
                        if (hasCategoryText && !previousCategoryHasText) {
                            leafIdx++;
                        }
                        previousCategoryHasText = hasCategoryText;
                    } else {
                        if (!isBlank(subcategoryText)) {
                            if (lastNonEmptySubcategory == null || !subcategoryText.equalsIgnoreCase(lastNonEmptySubcategory)) {
                                leafIdx++;
                                lastNonEmptySubcategory = subcategoryText;
                            }
                        }
                    }
                    if (leafIdx < 0 && !leafOrder.isEmpty()) {
                        leafIdx = 0;
                    }
                    // Use current leaf index (bounded) so first category/subcategory group maps to first leaf service
                    Services serviceForCompany = (!leafOrder.isEmpty() && leafIdx >= 0)
                            ? leafOrder.get(Math.min(leafIdx, leafOrder.size() - 1))
                            : null;
                    // Company handling (no description composition per new requirement)
                    if (!isBlank(companyName)) {
                        createCompany(companyName, email, telephone, mobile, whatsapp, telegram, website,
                                null, // industry not set from description anymore
                                serviceForCompany,
                                commonPassword, result);
                    }
                    result.rowsProcessed++;
                }
            }
        }
    }

    private static class ColumnsMap {
        int categoryIdx = -1;
        int subcategoryIdx = 0; // marker column, can be present or blank; default first col
        int companyIdx = 1;
        int telephoneIdx = 2;
        int mobileIdx = 3;
        int whatsappIdx = 4;
        int telegramIdx = 5;
        int emailIdx = 6;
        int websiteIdx = 7;
    }

    private ColumnsMap mapHeader(Row header) {
        ColumnsMap cm = new ColumnsMap();
        int last = header.getLastCellNum();
        for (int i = 0; i < last; i++) {
            String h = readString(header, i);
            assignHeaderColumn(cm, h, i);
        }
        return cm;
    }

    private ColumnsMap mapHeader(String[] headerCells) {
        ColumnsMap cm = new ColumnsMap();
        for (int i = 0; i < headerCells.length; i++) {
            assignHeaderColumn(cm, headerCells[i], i);
        }
        return cm;
    }

    private void assignHeaderColumn(ColumnsMap cm, String rawHeader, int index) {
        if (rawHeader == null) return;
        String key = rawHeader.trim().toLowerCase();
        if (key.isEmpty()) return;
        switch (key) {
            case "category", "catagory" -> cm.categoryIdx = index;
            case "subcategory", "subcatagory", "sub category", "sub catagory" -> cm.subcategoryIdx = index;
            case "company", "companyname", "company name", "name", "business", "organization" -> cm.companyIdx = index;
            case "telephone", "tel" -> cm.telephoneIdx = index;
            case "mobile", "mobiletelephone", "phone", "phone number", "phonenumber" -> cm.mobileIdx = index;
            case "whatsapp", "whats app", "whatsup no", "whatsapp no" -> cm.whatsappIdx = index;
            case "telegram", "telegram no" -> cm.telegramIdx = index;
            case "email", "e-mail" -> cm.emailIdx = index;
            case "website", "site", "web", "websiteaddress", "web site address" -> cm.websiteIdx = index;
            default -> {}
        }
    }

    private String readCsvByIndex(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return "";
        return trimNull(row[idx]);
    }

    private String readByIndex(Row row, int idx) {
        if (idx < 0) return "";
        return readString(row, idx);
    }

    private void processCsv(MultipartFile file, ServiceCategory category, String commonPassword, CombinedImportResult result) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine(); // header
            if (headerLine == null) return;
            String[] headerCells = simpleCsvSplit(headerLine);
            ColumnsMap cm = mapHeader(headerCells);
            // Pre-compute ordered leaf services for the first category
            List<Services> leafOrder = computeLeafServicesOrdered(category);
            int leafIdx = -1;
            boolean previousCategoryHasText = false;
            String lastNonEmptySubcategory = null;
            boolean groupByCategory = cm.categoryIdx >= 0;
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = simpleCsvSplit(line);
                String categoryText = readCsvByIndex(cols, cm.categoryIdx);
                String subcategoryText = readCsvByIndex(cols, cm.subcategoryIdx);
                String companyName = readCsvByIndex(cols, cm.companyIdx);
                String telephone = readCsvByIndex(cols, cm.telephoneIdx);
                String mobile = readCsvByIndex(cols, cm.mobileIdx);
                String whatsapp = readCsvByIndex(cols, cm.whatsappIdx);
                String telegram = readCsvByIndex(cols, cm.telegramIdx);
                String email = readCsvByIndex(cols, cm.emailIdx);
                String website = readCsvByIndex(cols, cm.websiteIdx);

                if (groupByCategory) {
                    boolean hasCategoryText = !isBlank(categoryText);
                    if (hasCategoryText && !previousCategoryHasText) {
                        leafIdx++;
                    }
                    previousCategoryHasText = hasCategoryText;
                } else {
                    if (!isBlank(subcategoryText)) {
                        if (lastNonEmptySubcategory == null || !subcategoryText.equalsIgnoreCase(lastNonEmptySubcategory)) {
                            leafIdx++;
                            lastNonEmptySubcategory = subcategoryText;
                        }
                    }
                }
                if (leafIdx < 0 && !leafOrder.isEmpty()) {
                    leafIdx = 0;
                }
                Services serviceForCompany = (!leafOrder.isEmpty() && leafIdx >= 0)
                        ? leafOrder.get(Math.min(leafIdx, leafOrder.size() - 1))
                        : null;

                if (!isBlank(companyName)) {
                    createCompany(companyName, email, telephone, mobile, whatsapp, telegram, website,
                            null,
                            serviceForCompany,
                            commonPassword, result);
                }
                result.rowsProcessed++;
            }
        }
    }

    
    private void createCompany(String companyName, String email, String telephone, String mobile,
                               String whatsapp, String telegram, String website, String industry,
                               Services serviceToLink,
                               String commonPassword, CombinedImportResult res) {
        try {
            User user;
            String resolvedEmail = sanitizeEmail(!isBlank(email) ? email : null);
            if (isBlank(resolvedEmail)) {
                resolvedEmail = generateUniqueFallbackEmail(companyName);
            }
            Optional<User> existingByEmail = userRepository.findByEmail(resolvedEmail);
            if (existingByEmail.isPresent()) {
                user = existingByEmail.get();
            } else {
                String phoneCandidate = pickPrimaryPhone(mobile, telephone);
                String uniquePhone = ensureUniquePhone(phoneCandidate);

                UserRegistrationRequest reg = new UserRegistrationRequest();
                reg.setName(companyName);
                reg.setEmail(resolvedEmail);
                reg.setPhoneNumber(uniquePhone);
                reg.setPassword(commonPassword);
                // If DTO supports role, set JOB_COMPANY; otherwise default registration may create USER and we'll update via profile
                try {
                    reg.getClass().getMethod("setRole", UserRole.class).invoke(reg, UserRole.JOB_COMPANY);
                } catch (Exception ignore) { /* role not present in DTO, ignored */ }
                user = userService.registerUser(reg);
            }
            // Ensure a Business entity exists and attach the catalog Service
            Business business = businessRepository.findFirstByOwner(user).orElse(null);
            if (business == null) {
                business = new Business();
                business.setOwner(user);
                business.setName(companyName);
                business.setEmail(user.getEmail());
                business.setPhoneNumber(user.getPhoneNumber());
                // Per requirement: do not populate description from contact fields
                business.setDescription(null);
                business.setWebsite(website);
                business.setIndustry(industry);
                business.setVerified(false);
                business.setFeatured(false);
            }
            // Update phone lists on every import (create or update)
            List<String> teleList = splitPhones(telephone);
            List<String> mobileList = splitPhones(mobile);
            business.setTelephoneNumbers(teleList == null || teleList.isEmpty() ? null : teleList);
            business.setMobileNumbers(mobileList == null || mobileList.isEmpty() ? null : mobileList);
            if (serviceToLink != null) {
                if (business.getServices() == null) {
                    business.setServices(new java.util.HashSet<>());
                }
                business.getServices().add(serviceToLink);
            } else {
                res.messages.add("Company '" + companyName + "': no service found to link");
            }
            Business saved = businessRepository.save(business);
            if (saved.getId() != null) {
                res.companiesCreated++;
            } else {
                res.messages.add("Company '" + companyName + "': business save returned null id");
            }
        } catch (Exception ex) {
            res.messages.add("Company '" + companyName + "': " + ex.getMessage());
            // Clear persistence context to avoid 'null id ... (don't flush the Session after an exception occurs)'
            try { if (entityManager != null) entityManager.clear(); } catch (Exception ignore) {}
        }
    }

    // Attempt to read a representative translation name for a service (first found) to reuse as industry label.
    // Service creation helpers removed per new requirement: categories and subcategories already exist.

    // Utilities
    private static String readString(Row row, int cellIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String get(String[] arr, int idx) { return idx < arr.length ? trimNull(arr[idx]) : ""; }
    private static String trimNull(String s) { return s == null ? "" : s.trim(); }

    private static String[] simpleCsvSplit(String line) {
        // Basic split handling quoted commas; good enough for unquoted sample
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }

    private String generateUniqueFallbackEmail(String companyName) {
        String baseLocal = companyName.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (baseLocal.isEmpty()) baseLocal = "company";
        String domain = "example.com";
        String candidate = baseLocal + "+import@" + domain;
        int i = 2;
        while (userRepository.existsByEmail(candidate)) {
            candidate = baseLocal + "+import" + i + "@" + domain;
            i++;
            if (i > 1000) break; // safety
        }
        return candidate;
    }

    private String sanitizeEmail(String raw) {
        if (isBlank(raw)) return null;
        String e = raw.trim();
        // Common excel quirks: spaces, multiple addresses separated by '/', ',', ';'
        e = e.replaceAll("\n", " ").replaceAll("\r", " ").trim();
        // take first token if multiple
        String[] parts = e.split("[;,/ ]+");
        if (parts.length > 0) e = parts[0];
        // strip angle brackets
        e = e.replaceAll("^<|>$", "");
        // simple sanity: must contain '@' and a dot after
        if (!e.contains("@") || !e.substring(e.indexOf('@')).contains(".")) {
            return null;
        }
        return e.toLowerCase();
    }

    private String ensureUniquePhone(String phoneCandidate) {
        String digits = phoneCandidate == null ? "" : phoneCandidate.replaceAll("[^0-9]", "");
        if (digits.length() < 10) {
            digits = "251000000000"; // default Ethiopian-like
        }
        String normalized = digits.startsWith("+") ? digits : (digits.startsWith("251") ? "+" + digits : "+" + digits);
        String candidate = normalized;
        int i = 2;
        while (userRepository.existsByPhoneNumber(candidate)) {
            String suffix = String.valueOf(i);
            String head = normalized.replaceAll("[^0-9]", "");
            // Keep within 13 digits: replace last digits with counter
            String truncated = head.length() > 13 ? head.substring(0, 13 - suffix.length()) : head.substring(0, Math.max(0, head.length() - suffix.length()));
            candidate = "+" + truncated + suffix;
            i++;
            if (i > 1000) break; // safety
        }
        return candidate;
    }

    private String pickPrimaryPhone(String mobileRaw, String telephoneRaw) {
        // Prefer first part of mobile, else first part of telephone
        String m = firstPhonePart(mobileRaw);
        if (!isBlank(m)) return m;
        String t = firstPhonePart(telephoneRaw);
        if (!isBlank(t)) return t;
        return "+251000000000";
    }

    private List<String> splitPhones(String phonesRaw) {
        if (isBlank(phonesRaw)) return null;
        String[] parts = phonesRaw.split("[/,;\\s]+");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p == null ? null : p.trim();
            if (!isBlank(trimmed)) list.add(trimmed);
        }
        return list;
    }

    private String firstPhonePart(String raw) {
        if (isBlank(raw)) return null;
        String[] parts = raw.split("[/,;\\s]+");
        for (String p : parts) {
            String trimmed = p == null ? null : p.trim();
            if (!isBlank(trimmed)) return trimmed;
        }
        return null;
    }

    private List<Services> computeLeafServicesOrdered(ServiceCategory category) {
        // Load all services under the category ordered by displayOrder then id
        List<Services> all = servicesRepo.findByCategoryOrderByDisplayOrderAsc(category);
        Map<Long, List<Services>> childrenByParent = new HashMap<>();
        List<Services> roots = new ArrayList<>();
        for (Services s : all) {
            Long pid = s.getServiceId();
            if (pid == null) {
                roots.add(s);
            } else {
                childrenByParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(s);
            }
        }
        Comparator<Services> cmp = Comparator
                .comparing((Services s) -> s.getDisplayOrder() == null ? Long.MAX_VALUE : s.getDisplayOrder())
                .thenComparing(Services::getId);
        roots.sort(cmp);
        for (List<Services> list : childrenByParent.values()) list.sort(cmp);

        List<Services> leaves = new ArrayList<>();
        for (Services r : roots) {
            dfsCollectLeaves(r, childrenByParent, leaves);
        }
        return leaves;
    }

    private void dfsCollectLeaves(Services node, Map<Long, List<Services>> childrenByParent, List<Services> acc) {
        List<Services> kids = childrenByParent.get(node.getId());
        if (kids == null || kids.isEmpty()) {
            acc.add(node);
            return;
        }
        for (Services c : kids) dfsCollectLeaves(c, childrenByParent, acc);
    }

    // No-op helper removed; avoid touching lazy-loaded collections during import
}
