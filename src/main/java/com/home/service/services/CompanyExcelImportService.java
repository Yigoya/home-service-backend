package com.home.service.services;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.UserService;
import com.home.service.dto.UserRegistrationRequest;
import com.home.service.models.Business;
import com.home.service.models.Services;
import com.home.service.models.User;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ServiceTranslationRepository;

@Service
public class CompanyExcelImportService {

    private final UserService userService;
    private final BusinessRepository businessRepository;
    private final ServiceTranslationRepository serviceTranslationRepository;

    public CompanyExcelImportService(UserService userService,
                                     BusinessRepository businessRepository,
                                     ServiceTranslationRepository serviceTranslationRepository) {
        this.userService = userService;
        this.businessRepository = businessRepository;
        this.serviceTranslationRepository = serviceTranslationRepository;
    }

    public static class ImportCompaniesResult {
        public int created;
        public int skipped;
        public java.util.List<String> messages = new java.util.ArrayList<>();
    }

    @Transactional
    public ImportCompaniesResult importCompanies(MultipartFile file, String commonPassword, Integer sheetIndex) {
        ImportCompaniesResult summary = new ImportCompaniesResult();
        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
            int idx = sheetIndex != null ? sheetIndex : 0;
            if (idx < 0 || idx >= wb.getNumberOfSheets()) {
                throw new IllegalArgumentException("Invalid sheet index: " + idx);
            }
            Sheet sheet = wb.getSheetAt(idx);

            // Expected header:
            // CATEGORY, SUBCATAGORY, CompanyName, Telephone, MobileTelephone, Whatsup No, Telegram No, Email, WebSiteAddress
            Iterator<Row> it = sheet.rowIterator();
            if (!it.hasNext()) return summary; // skip if empty
            it.next(); // header

            String lastCategory = null; // not used directly for company creation, but available if mapping needed
            String lastSubcategory = null; // remember last explicit subcategory to apply to following companies when left blank
            while (it.hasNext()) {
                Row row = it.next();
                String category = readString(row, 0);
                String subcategory = readString(row, 1);
                String companyName = readString(row, 2);
                String telephone = readString(row, 3);
                String mobile = readString(row, 4);
                String whatsapp = readString(row, 5);
                String telegram = readString(row, 6);
                String email = readString(row, 7);
                String website = readString(row, 8);

                if (isBlank(companyName)) {
                    if (!isBlank(category)) {
                        lastCategory = category; // track last category; could map to industry
                    }
                    continue; // skip rows without company name
                }

                // If subcategory cell blank but we previously had one under the same (or continuing) category, reuse it.
                if (isBlank(subcategory) && !isBlank(lastSubcategory)) {
                    subcategory = lastSubcategory; // reuse previous subcategory grouping
                    summary.messages.add("Row " + row.getRowNum() + ": reused subcategory '" + subcategory + "'");
                }

                // Track most recent non-empty subcategory after potential reuse logic
                if (!isBlank(subcategory)) {
                    lastSubcategory = subcategory; // update memory of last subcategory
                }

                // Build registration request
                UserRegistrationRequest reg = new UserRegistrationRequest();
                reg.setName(companyName);
                String sanitized = sanitizeEmail(email);
                reg.setEmail(!isBlank(sanitized) ? sanitized : generateFallbackEmail(companyName));
                reg.setPhoneNumber(!isBlank(mobile) ? mobile : (!isBlank(telephone) ? telephone : "+251000000000"));
                reg.setPassword(commonPassword);
                reg.setRole(UserRole.JOB_COMPANY);

                try {
                    // Register user using the same path as /auth/register
                    User user = userService.registerUser(reg);

                    // Create or update Business for this user
                    Business business = businessRepository.findFirstByOwner(user).orElseGet(Business::new);
                    business.setOwner(user);
                    business.setName(companyName);
                    business.setEmail(reg.getEmail());
                    business.setPhoneNumber(reg.getPhoneNumber());
                    business.setWebsite(website);
                    // Prefer subcategory as more specific industry, fallback to category/lastCategory
                    String industry = !isBlank(subcategory) ? subcategory : (!isBlank(category) ? category : lastCategory);
                    business.setIndustry(industry);
                    // Append whatsapp/telegram/telephone/mobile into description
                    StringBuilder desc = new StringBuilder();
                    if (!isBlank(telephone)) desc.append("Tel: ").append(telephone).append(". ");
                    if (!isBlank(mobile)) desc.append("Mobile: ").append(mobile).append(". ");
                    if (!isBlank(whatsapp)) desc.append("WhatsApp: ").append(whatsapp).append(". ");
                    if (!isBlank(telegram)) desc.append("Telegram: ").append(telegram).append(". ");
                    business.setDescription(desc.length() > 0 ? desc.toString().trim() : null);
                    // Ensure booleans are set to avoid DB not-null
                    business.setVerified(false);
                    business.setFeatured(false);

                    // Link to an existing catalog Service if translation name matches
                    Services linked = null;
                    if (!isBlank(subcategory)) {
                        linked = serviceTranslationRepository.findFirstByNameIgnoreCase(subcategory)
                                .map(ServiceTranslation::getService)
                                .orElse(null);
                    }
                    if (linked == null && !isBlank(category)) {
                        linked = serviceTranslationRepository.findFirstByNameIgnoreCase(category)
                                .map(ServiceTranslation::getService)
                                .orElse(null);
                    }
                    if (linked != null) {
                        business.getServices().add(linked);
                    }

                    Business saved = businessRepository.save(business);
                    if (saved.getId() != null) {
                        summary.created++;
                    } else {
                        summary.messages.add("Row " + row.getRowNum() + ": business save returned null id for '" + companyName + "'");
                    }
                } catch (Exception ex) {
                    summary.skipped++;
                    summary.messages.add("Row " + row.getRowNum() + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import companies: " + e.getMessage(), e);
        }
        return summary;
    }

    private static String readString(Row row, int cellIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long)cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String generateFallbackEmail(String companyName) {
        String base = companyName.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) base = "company";
        return base + "+import@example.com";
    }

    private static String sanitizeEmail(String raw) {
        if (raw == null) return null;
        String e = raw.trim();
        e = e.replaceAll("\n|\r", " ").trim();
        String[] parts = e.split("[;,/ ]+");
        if (parts.length > 0) e = parts[0];
        e = e.replaceAll("^<|>$", "");
        if (!e.contains("@")) return null;
        int at = e.indexOf('@');
        if (at <= 0 || at >= e.length() - 1) return null;
        if (!e.substring(at).contains(".")) return null;
        return e.toLowerCase();
    }
}
