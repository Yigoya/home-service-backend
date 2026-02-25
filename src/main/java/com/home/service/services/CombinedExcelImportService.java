package com.home.service.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;

import com.home.service.Service.UserService;
import com.home.service.dto.UserRegistrationRequest;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceCategoryTranslation;
import com.home.service.models.Services;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.User;
import com.home.service.models.enums.UserRole;
import com.home.service.models.enums.EthiopianLanguage;
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
        public int categoriesCreated;
        public int companiesCreated;
        public int rowsProcessed;
        public List<String> messages = new ArrayList<>();
    }

    public CombinedImportResult importAll(MultipartFile file, Long defaultCategoryId, String commonPassword, Integer sheetIndex) {
        return importAll(file, defaultCategoryId, commonPassword, sheetIndex, null, null);
    }

    // New overload: supports multiple sheet indices and process-all option
    public CombinedImportResult importAll(MultipartFile file, Long defaultCategoryId, String commonPassword,
                                          Integer sheetIndex, String sheetIndicesCsv, Boolean processAllSheets) {
        if (commonPassword == null || commonPassword.isBlank()) {
            throw new IllegalArgumentException("commonPassword is required");
        }

        CombinedImportResult result = new CombinedImportResult();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try {
            if (filename.endsWith(".csv")) {
                processCsv(file, commonPassword, result);
            } else {
                int[] indices;
                try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
                    int sheetCount = wb.getNumberOfSheets();
                    if (sheetCount == 0) {
                        throw new IllegalArgumentException("No sheets found in workbook");
                    }
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
                processXlsxMulti(file, commonPassword, indices, result);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import: " + e.getMessage(), e);
        }
        return result;
    }

    private void processXlsxMulti(MultipartFile file, String commonPassword, int[] sheetIndices, CombinedImportResult result) throws Exception {
        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
            ImportState state = new ImportState();
            for (int sheetIdx : sheetIndices) {
                if (sheetIdx < 0 || sheetIdx >= wb.getNumberOfSheets()) {
                    throw new IllegalArgumentException("Invalid sheet index: " + sheetIdx);
                }
                Sheet sheet = wb.getSheetAt(sheetIdx);
                Iterator<Row> it = sheet.rowIterator();
                if (!it.hasNext()) continue;
                Row header = it.next();
                ColumnsMap cm = mapHeader(header);
                while (it.hasNext()) {
                    Row row = it.next();
                    processStructuredRow(
                            readByIndex(row, cm.levelIdx),
                            readByIndex(row, cm.categoryIdx),
                            readByIndex(row, cm.companyIdx),
                            readByIndex(row, cm.telephoneIdx),
                            readByIndex(row, cm.mobileIdx),
                            readByIndex(row, cm.whatsappIdx),
                            readByIndex(row, cm.telegramIdx),
                            readByIndex(row, cm.emailIdx),
                            readByIndex(row, cm.websiteIdx),
                            state,
                            commonPassword,
                            result
                    );
                }
            }
        }
    }

    private static class ColumnsMap {
        int levelIdx = 0;
        int categoryIdx = 1;
        int companyIdx = 2;
        int telephoneIdx = 3;
        int mobileIdx = 4;
        int whatsappIdx = 5;
        int telegramIdx = 6;
        int emailIdx = 7;
        int websiteIdx = 8;
    }

    private static class ImportState {
        ServiceCategory currentCategory;
        Services currentLevel1;
        Services currentLevel2;
        Map<String, ServiceCategory> categoriesByKey = new HashMap<>();
        Map<String, Services> level1ByKey = new HashMap<>();
        Map<String, Services> level2ByKey = new HashMap<>();
        long categoryOrderCounter = 0;
        long serviceOrderCounter = 0;
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
            case "level", "lvl", "lebel" -> cm.levelIdx = index;
            case "category/subcategory", "category", "subcategory", "sub category", "category or subcategory" -> cm.categoryIdx = index;
            case "companys name", "companys", "company", "company name", "name", "business", "organization" -> cm.companyIdx = index;
            case "telephone", "tel" -> cm.telephoneIdx = index;
            case "mobiletelephone", "mobile", "phone", "phone number", "phonenumber" -> cm.mobileIdx = index;
            case "whatsup no", "whatsapp", "whatsapp no", "whats app" -> cm.whatsappIdx = index;
            case "telegram", "telegram no" -> cm.telegramIdx = index;
            case "email", "e-mail" -> cm.emailIdx = index;
            case "websiteaddress", "web site address", "website", "site", "web" -> cm.websiteIdx = index;
            default -> {}
        }
    }

    private void processStructuredRow(String levelRaw,
                                      String categoryOrSub,
                                      String companyName,
                                      String telephone,
                                      String mobile,
                                      String whatsapp,
                                      String telegram,
                                      String email,
                                      String website,
                                      ImportState state,
                                      String commonPassword,
                                      CombinedImportResult res) {

        Integer level = parseLevel(levelRaw);

        if (level != null) {
            switch (level) {
                case 0 -> {
                    if (isBlank(categoryOrSub)) {
                        res.messages.add("Level 0 row missing CATEGORY/SUBCATEGORY name");
                    } else {
                        state.currentCategory = getOrCreateCategory(categoryOrSub, state, res);
                        state.currentLevel1 = null;
                        state.currentLevel2 = null;
                    }
                }
                case 1 -> {
                    if (state.currentCategory == null) {
                        res.messages.add("Level 1 row appeared before any Level 0 category: " + categoryOrSub);
                    } else if (isBlank(categoryOrSub)) {
                        res.messages.add("Level 1 row missing CATEGORY/SUBCATEGORY name");
                    } else {
                        state.currentLevel1 = getOrCreateService(categoryOrSub, state.currentCategory, null, state, res);
                        state.currentLevel2 = null;
                    }
                }
                case 2 -> {
                    if (state.currentCategory == null) {
                        res.messages.add("Level 2 row appeared before any Level 0 category: " + categoryOrSub);
                    } else if (state.currentLevel1 == null) {
                        res.messages.add("Level 2 row appeared before any Level 1 service: " + categoryOrSub);
                    } else if (!isBlank(categoryOrSub)) {
                        state.currentLevel2 = getOrCreateService(categoryOrSub, state.currentCategory, state.currentLevel1, state, res);
                    } else if (state.currentLevel2 == null) {
                        res.messages.add("Level 2 row missing CATEGORY/SUBCATEGORY name");
                    }
                }
                default -> res.messages.add("Unsupported level value: " + levelRaw);
            }
        }

        boolean hasCompanyData = !isBlank(companyName) || !isBlank(telephone) || !isBlank(mobile)
                || !isBlank(whatsapp) || !isBlank(telegram) || !isBlank(email) || !isBlank(website);

        Services targetService = state.currentLevel2 != null ? state.currentLevel2 : state.currentLevel1;

        if (hasCompanyData) {
            if (targetService == null) {
                res.messages.add("Company '" + companyName + "' has no service context (need Level 1/2 first)");
            } else if (!isBlank(companyName)) {
                createCompany(companyName, email, telephone, mobile, whatsapp, telegram, website,
                        null, targetService, commonPassword, res);
            }
        }
        res.rowsProcessed++;
    }

    private Integer parseLevel(String levelRaw) {
        if (isBlank(levelRaw)) return null;
        try {
            int v = Integer.parseInt(levelRaw.trim());
            return switch (v) { case 0, 1, 2 -> v; default -> null; };
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private ServiceCategory getOrCreateCategory(String name, ImportState state, CombinedImportResult res) {
        String key = normalizeKey(name);
        ServiceCategory cached = state.categoriesByKey.get(key);
        if (cached != null) return cached;

        ServiceCategory category = new ServiceCategory();
        category.setIsMobileCategory(false);
        category.setOrder(++state.categoryOrderCounter);

        ServiceCategoryTranslation tr = new ServiceCategoryTranslation();
        tr.setCategory(category);
        tr.setLang(EthiopianLanguage.ENGLISH);
        tr.setName(name.trim());
        tr.setDescription("");

        category.setTranslations(new java.util.HashSet<>());
        category.getTranslations().add(tr);

        ServiceCategory saved = categoryRepo.save(category);
        state.categoriesByKey.put(key, saved);
        state.currentCategory = saved;
        res.categoriesCreated++;
        return saved;
    }

    private Services getOrCreateService(String name, ServiceCategory category, Services parent, ImportState state, CombinedImportResult res) {
        String key = normalizeKey(name) + "|cat:" + (category != null && category.getId() != null ? category.getId() : category.hashCode())
                + "|parent:" + (parent != null && parent.getId() != null ? parent.getId() : (parent == null ? "root" : parent.hashCode()));

        Map<String, Services> cache = parent == null ? state.level1ByKey : state.level2ByKey;
        Services cached = cache.get(key);
        if (cached != null) return cached;

        Services svc = new Services();
        svc.setCategory(category);
        svc.setServiceId(parent != null ? parent.getId() : null);
        svc.setDisplayOrder(++state.serviceOrderCounter);

        ServiceTranslation tr = new ServiceTranslation();
        tr.setService(svc);
        tr.setLang(EthiopianLanguage.ENGLISH);
        tr.setName(name.trim());
        tr.setDescription("");

        svc.setTranslations(new java.util.HashSet<>());
        svc.getTranslations().add(tr);

        Services saved = servicesRepo.save(svc);
        cache.put(key, saved);
        res.servicesCreated++;
        return saved;
    }

    private String normalizeKey(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String readCsvByIndex(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return "";
        return trimNull(row[idx]);
    }

    private String readByIndex(Row row, int idx) {
        if (idx < 0) return "";
        return readString(row, idx);
    }

    private void processCsv(MultipartFile file, String commonPassword, CombinedImportResult result) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) return;
            String[] headerCells = simpleCsvSplit(headerLine);
            ColumnsMap cm = mapHeader(headerCells);
            ImportState state = new ImportState();
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = simpleCsvSplit(line);
                processStructuredRow(
                        readCsvByIndex(cols, cm.levelIdx),
                        readCsvByIndex(cols, cm.categoryIdx),
                        readCsvByIndex(cols, cm.companyIdx),
                        readCsvByIndex(cols, cm.telephoneIdx),
                        readCsvByIndex(cols, cm.mobileIdx),
                        readCsvByIndex(cols, cm.whatsappIdx),
                        readCsvByIndex(cols, cm.telegramIdx),
                        readCsvByIndex(cols, cm.emailIdx),
                        readCsvByIndex(cols, cm.websiteIdx),
                        state,
                        commonPassword,
                        result
                );
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
                user = registerWithFallbacks(companyName, resolvedEmail, mobile, telephone, commonPassword, res);
            }

            // Ensure a Business entity exists and attach the catalog Service
            Business business = businessRepository.findFirstByOwner(user).orElse(null);
            if (business == null) {
                business = new Business();
                business.setOwner(user);
                business.setName(companyName);
                business.setEmail(user.getEmail());
                business.setPhoneNumber(user.getPhoneNumber());
                business.setDescription(null);
                business.setWebsite(website);
                business.setIndustry(industry);
                business.setVerified(false);
                business.setFeatured(false);
                business.setTaxId(generateTaxId(companyName));
                business.setLocalDistributionNetwork(false);
            } else {
                if (isBlank(business.getTaxId())) {
                    business.setTaxId(generateTaxId(companyName));
                }
                if (isBlank(business.getPhoneNumber())) {
                    business.setPhoneNumber(user.getPhoneNumber());
                }
                if (isBlank(business.getEmail())) {
                    business.setEmail(user.getEmail());
                }
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
            try { if (entityManager != null) entityManager.clear(); } catch (Exception ignore) {}
        }
    }

    private User registerWithFallbacks(String companyName, String email, String mobile, String telephone, String commonPassword, CombinedImportResult res) {
        String basePhone = pickPrimaryPhone(mobile, telephone);
        String phoneCandidate = ensureUniquePhone(basePhone);
        String emailCandidate = email;
        int attempts = 0;
        while (attempts < 5) {
            attempts++;
            try {
                UserRegistrationRequest reg = new UserRegistrationRequest();
                reg.setName(companyName);
                reg.setEmail(emailCandidate);
                reg.setPhoneNumber(phoneCandidate);
                reg.setPassword(commonPassword);
                try {
                    reg.getClass().getMethod("setRole", UserRole.class).invoke(reg, UserRole.BUSINESS);
                } catch (Exception ignore) { }
                return userService.registerUser(reg);
            } catch (DataIntegrityViolationException dive) {
                String msg = dive.getMostSpecificCause() != null ? dive.getMostSpecificCause().getMessage() : dive.getMessage();
                if (msg != null && msg.contains("users_pkey")) {
                    alignUserSequence();
                    continue;
                }
                if (msg != null && msg.toLowerCase().contains("phone number already in use")) {
                    phoneCandidate = ensureUniquePhone(generateRandomPhone());
                    continue;
                }
                if (msg != null && msg.toLowerCase().contains("email")) {
                    emailCandidate = generateUniqueFallbackEmail(companyName + attempts);
                    continue;
                }
                throw dive;
            } catch (Exception ex) {
                String lower = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                if (lower.contains("phone number already in use")) {
                    phoneCandidate = ensureUniquePhone(generateRandomPhone());
                    continue;
                }
                if (lower.contains("email")) {
                    emailCandidate = generateUniqueFallbackEmail(companyName + attempts);
                    continue;
                }
                // Unknown; rethrow to bubble up
                throw ex;
            }
        }
        throw new RuntimeException("Failed to register user after retries for company: " + companyName + ", last email: " + emailCandidate);
    }

    private void alignUserSequence() {
        try {
            entityManager.createNativeQuery("select setval('users_id_seq', (select coalesce(max(id),0)+1 from users), false)").executeUpdate();
        } catch (Exception ignore) {
            // If sequence name differs, ignore; next attempt may still work
        }
    }

    private String generateTaxId(String companyName) {
        String base = companyName == null ? "TIN" : companyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (base.length() > 12) base = base.substring(0, 12);
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        return (base.isEmpty() ? "TIN" : base) + "-" + suffix;
    }

    private String generateRandomPhone() {
        String millis = String.valueOf(System.currentTimeMillis());
        String last9 = millis.substring(Math.max(0, millis.length() - 9));
        return "+2519" + String.format("%08d", Long.parseLong(last9) % 100000000L);
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

}
