package com.home.service.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.UserService;
import com.home.service.dto.UserRegistrationRequest;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.models.User;
import com.home.service.models.enums.UserRole;
import com.home.service.models.Business;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.ServiceTranslationRepository;
import com.home.service.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class CombinedExcelImportService {

    private final ServiceCategoryRepository categoryRepo;
    private final ServiceRepository servicesRepo;
    private final ServiceTranslationRepository translationRepo;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CombinedExcelImportService(ServiceCategoryRepository categoryRepo,
                                      ServiceRepository servicesRepo,
                                      ServiceTranslationRepository translationRepo,
                                      UserService userService,
                                      UserRepository userRepository,
                                      BusinessRepository businessRepository) {
        this.categoryRepo = categoryRepo;
        this.servicesRepo = servicesRepo;
        this.translationRepo = translationRepo;
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
        if (defaultCategoryId == null) {
            throw new IllegalArgumentException("defaultCategoryId is required");
        }
        if (commonPassword == null || commonPassword.isBlank()) {
            throw new IllegalArgumentException("commonPassword is required");
        }
        ServiceCategory category = categoryRepo.findById(defaultCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("ServiceCategory not found: " + defaultCategoryId));

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
        if (defaultCategoryId == null) {
            throw new IllegalArgumentException("defaultCategoryId is required");
        }
        if (commonPassword == null || commonPassword.isBlank()) {
            throw new IllegalArgumentException("commonPassword is required");
        }
        ServiceCategory category = categoryRepo.findById(defaultCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("ServiceCategory not found: " + defaultCategoryId));

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
            for (int sheetIdx : sheetIndices) {
                if (sheetIdx < 0 || sheetIdx >= wb.getNumberOfSheets()) {
                    throw new IllegalArgumentException("Invalid sheet index: " + sheetIdx);
                }
                Sheet sheet = wb.getSheetAt(sheetIdx);
                Iterator<Row> it = sheet.rowIterator();
                if (!it.hasNext()) continue; // empty sheet
                Row header = it.next();
                ColumnsMap cm = mapHeader(header);

                Services currentParent = null;
                Services currentChild = null;
                // Preserve the last non-empty subcategory service so that subsequent rows
                // (where the user leaves the subcategory cell blank for brevity) still
                // attach companies to the same subcategory. This matches common Excel
                // grouping practice.
                Services lastChildService = null;
                while (it.hasNext()) {
                    Row row = it.next();
                    String categoryText = readByIndex(row, cm.categoryIdx);
                    String subcategoryText = cm.subcategoryIdx >= 0 ? readByIndex(row, cm.subcategoryIdx) : "";
                    String companyName = readByIndex(row, cm.companyIdx);
                    String telephone = readByIndex(row, cm.telephoneIdx);
                    String mobile = readByIndex(row, cm.mobileIdx);
                    String whatsapp = readByIndex(row, cm.whatsappIdx);
                    String telegram = readByIndex(row, cm.telegramIdx);
                    String email = readByIndex(row, cm.emailIdx);
                    String website = readByIndex(row, cm.websiteIdx);

                    // Services handling
                    // Subcategory handling with persistence of last child
                    if (!isBlank(categoryText)) {
                        // New category encountered: reset parent and (unless a subcategory provided) clear last child
                        currentParent = findOrCreateTopLevelServiceByName(categoryText, category, result);
                        if (!isBlank(subcategoryText)) {
                            currentChild = findOrCreateChildServiceByName(subcategoryText, category, currentParent, result);
                            lastChildService = currentChild; // remember for subsequent blank rows
                            result.messages.add("Row " + row.getRowNum() + ": set subcategory '" + subcategoryText + "'");
                        } else {
                            currentChild = null;
                            lastChildService = null; // starting a fresh category group with no subcategory
                        }
                    } else {
                        if (!isBlank(subcategoryText) && currentParent != null) {
                            // Explicit new subcategory under existing parent
                            currentChild = findOrCreateChildServiceByName(subcategoryText, category, currentParent, result);
                            lastChildService = currentChild;
                            result.messages.add("Row " + row.getRowNum() + ": new subcategory '" + subcategoryText + "'");
                        } else {
                            // Blank category & blank subcategory: reuse last child if present
                            currentChild = lastChildService;
                            if (currentChild != null) {
                                result.messages.add("Row " + row.getRowNum() + ": reused previous subcategory service id=" + currentChild.getId());
                            }
                        }
                    }

                    // Company handling
                    if (!isBlank(companyName)) {
            Services serviceForCompany = (currentChild != null ? currentChild : (lastChildService != null ? lastChildService : currentParent));
            // Industry: prefer current (or persisted) subcategory, then current category
            String effectiveSub = !isBlank(subcategoryText) ? subcategoryText : (currentChild == null && lastChildService != null ? readServiceTranslationName(lastChildService) : null);
            String industry = !isBlank(effectiveSub) ? effectiveSub : (!isBlank(categoryText) ? categoryText : null);
                        createCompany(companyName, email, telephone, mobile, whatsapp, telegram, website,
                                industry,
                                serviceForCompany,
                                commonPassword, result);
                    }
                    result.rowsProcessed++;
                }
            }
        }
    }

    private static class ColumnsMap {
        int categoryIdx = 0;
        int subcategoryIdx = 1; // -1 means not present
        int companyIdx = 2;
        int telephoneIdx = 3;
        int mobileIdx = 4;
        int whatsappIdx = 5;
        int telegramIdx = 6;
        int emailIdx = 7;
        int websiteIdx = 8;
    }

    private ColumnsMap mapHeader(Row header) {
        ColumnsMap cm = new ColumnsMap();
        // Build lowercase header map
        int last = header.getLastCellNum();
        for (int i = 0; i < last; i++) {
            String h = readString(header, i);
            if (h == null) continue;
            String key = h.trim().toLowerCase();
            switch (key) {
                case "category", "catagory" -> cm.categoryIdx = i;
                case "subcategory", "subcatagory", "sub category", "sub catagory" -> cm.subcategoryIdx = i;
                case "company", "companyname", "company name", "name", "business", "organization" -> cm.companyIdx = i;
                case "telephone", "tel" -> cm.telephoneIdx = i;
                case "mobile", "phone", "phone number", "phonenumber" -> cm.mobileIdx = i;
                case "whatsapp", "whats app" -> cm.whatsappIdx = i;
                case "telegram" -> cm.telegramIdx = i;
                case "email", "e-mail" -> cm.emailIdx = i;
                case "website", "site", "web" -> cm.websiteIdx = i;
                default -> {}
            }
        }
        // If header didn't include subcategory, mark as not present
        String maybeSub = readString(header, cm.subcategoryIdx);
        if (isBlank(maybeSub) || !(maybeSub.equalsIgnoreCase("subcategory") || maybeSub.equalsIgnoreCase("subcatagory")
                || maybeSub.equalsIgnoreCase("sub category") || maybeSub.equalsIgnoreCase("sub catagory"))) {
            // Only treat as missing if header clearly didn't name it and index equals default position
            // Better: scan if any cell matches; already done above, so if still default 1 and header cell not matching, then set -1
            if (cm.subcategoryIdx == 1) cm.subcategoryIdx = -1;
        }
        return cm;
    }

    private String readByIndex(Row row, int idx) {
        if (idx < 0) return "";
        return readString(row, idx);
    }

    private void processCsv(MultipartFile file, ServiceCategory category, String commonPassword, CombinedImportResult result) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // header
            if (line == null) return;
            Services currentParent = null;
            Services currentChild = null;
            Services lastChildService = null;
            while ((line = br.readLine()) != null) {
                String[] cols = simpleCsvSplit(line);
                // Ensure at least 9 columns
                String categoryText = get(cols, 0);
                String subcategoryText = get(cols, 1);
                String companyName = get(cols, 2);
                String telephone = get(cols, 3);
                String mobile = get(cols, 4);
                String whatsapp = get(cols, 5);
                String telegram = get(cols, 6);
                String email = get(cols, 7);
                String website = get(cols, 8);

                // Subcategory handling with persistence of last child
                if (!isBlank(categoryText)) {
                    currentParent = findOrCreateTopLevelServiceByName(categoryText, category, result);
                    if (!isBlank(subcategoryText)) {
                        currentChild = findOrCreateChildServiceByName(subcategoryText, category, currentParent, result);
                        lastChildService = currentChild;
                        result.messages.add("CSV row: set subcategory '" + subcategoryText + "'");
                    } else {
                        currentChild = null;
                        lastChildService = null;
                    }
                } else {
                    if (!isBlank(subcategoryText) && currentParent != null) {
                        currentChild = findOrCreateChildServiceByName(subcategoryText, category, currentParent, result);
                        lastChildService = currentChild;
                        result.messages.add("CSV row: new subcategory '" + subcategoryText + "'");
                    } else {
                        currentChild = lastChildService;
                        if (currentChild != null) {
                            result.messages.add("CSV row: reused previous subcategory service id=" + currentChild.getId());
                        }
                    }
                }

                if (!isBlank(companyName)) {
                    Services serviceForCompany = (currentChild != null ? currentChild : (lastChildService != null ? lastChildService : currentParent));
                    String effectiveSub = !isBlank(subcategoryText) ? subcategoryText : (currentChild == null && lastChildService != null ? null : null);
                    String industry = !isBlank(effectiveSub) ? effectiveSub : (!isBlank(categoryText) ? categoryText : null);
                    createCompany(companyName, email, telephone, mobile, whatsapp, telegram, website,
                            industry,
                            serviceForCompany,
                            commonPassword, result);
                }
                result.rowsProcessed++;
            }
        }
    }

    private Services findOrCreateTopLevelServiceByName(String name, ServiceCategory category, CombinedImportResult res) {
        Optional<ServiceTranslation> existing = translationRepo
                .findFirstByNameIgnoreCaseAndService_Category_IdAndService_ServiceIdIsNull(name, category.getId());
        if (existing.isPresent()) {
            return existing.get().getService();
        }
        Services service = new Services();
        service.setCategory(category);
        service = servicesRepo.save(service);
        ServiceTranslation t = new ServiceTranslation();
        t.setName(name);
        t.setService(service);
        translationRepo.save(t);
        res.servicesCreated++;
        return service;
    }

    private Services findOrCreateChildServiceByName(String name, ServiceCategory category, Services parent, CombinedImportResult res) {
        Optional<ServiceTranslation> existing = translationRepo
                .findFirstByNameIgnoreCaseAndService_Category_IdAndService_ServiceId(name, category.getId(), parent.getId());
        if (existing.isPresent()) return existing.get().getService();
        Services child = new Services();
        child.setCategory(category);
        child.setServiceId(parent.getId());
        child = servicesRepo.save(child);
        ServiceTranslation t = new ServiceTranslation();
        t.setName(name);
        t.setService(child);
        translationRepo.save(t);
        res.servicesCreated++;
        return child;
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
                String phoneCandidate = !isBlank(mobile) ? mobile : (!isBlank(telephone) ? telephone : "+251000000000");
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
                StringBuilder desc = new StringBuilder();
                if (!isBlank(telephone)) desc.append("Tel: ").append(telephone).append(". ");
                if (!isBlank(mobile)) desc.append("Mobile: ").append(mobile).append(". ");
                if (!isBlank(whatsapp)) desc.append("WhatsApp: ").append(whatsapp).append(". ");
                if (!isBlank(telegram)) desc.append("Telegram: ").append(telegram).append(". ");
                business.setDescription(desc.length() > 0 ? desc.toString().trim() : null);
                business.setWebsite(website);
                business.setIndustry(industry);
                business.setVerified(false);
                business.setFeatured(false);
            }
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
    private String readServiceTranslationName(Services service) {
        if (service == null) return null;
        try {
            // Use existing repository to fetch one translation by service id
            Optional<ServiceTranslation> t = translationRepo.findFirstByNameIgnoreCaseAndService_Category_IdAndService_ServiceIdIsNull("__dummy__", -1L);
        } catch (Exception ignore) {
            // Fallback: nothing; we avoid loading full collection to keep import lean.
        }
        // Without an efficient way, simply return null; industry will fallback to category.
        return null;
    }

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

    // No-op helper removed; avoid touching lazy-loaded collections during import
}
