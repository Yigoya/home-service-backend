package com.home.service.services;

import java.io.InputStream;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.ServiceTranslationRepository;

@Service
public class ExcelImportService {

    private final ServiceCategoryRepository categoryRepo;
    private final ServiceRepository servicesRepo;
    private final ServiceTranslationRepository translationRepo;

    public ExcelImportService(ServiceCategoryRepository categoryRepo,
                              ServiceRepository servicesRepo,
                              ServiceTranslationRepository translationRepo) {
        this.categoryRepo = categoryRepo;
        this.servicesRepo = servicesRepo;
        this.translationRepo = translationRepo;
    }

    public static class ImportResult {
        public int categoriesCreated;
        public int servicesCreated;
        public List<String> messages = new ArrayList<>();
    }

    @Transactional
    public ImportResult importCompanyExcel(MultipartFile file,
                                           int[] sheetIndexes,
                                           boolean[] sheetHasSubcategory,
                                           Long defaultCategoryIdIfNone) {
        if (sheetIndexes == null || sheetIndexes.length == 0) {
            throw new IllegalArgumentException("sheetIndexes required");
        }
        ImportResult result = new ImportResult();
        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
            for (int i = 0; i < sheetIndexes.length; i++) {
                int sheetIdx = sheetIndexes[i];
                boolean hasSub = sheetHasSubcategory != null && i < sheetHasSubcategory.length && sheetHasSubcategory[i];
                if (sheetIdx < 0 || sheetIdx >= wb.getNumberOfSheets()) {
                    result.messages.add("Skip invalid sheet index " + sheetIdx);
                    continue;
                }
                Sheet sheet = wb.getSheetAt(sheetIdx);
                processSheet(sheet, hasSub, defaultCategoryIdIfNone, result);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import Excel: " + e.getMessage(), e);
        }
        return result;
    }

    private void processSheet(Sheet sheet, boolean sheetHasSubcategory, Long defaultCategoryIdIfNone, ImportResult result) {
        // Expected header:
        // CATEGORY, SUBCATAGORY, CompanyName, Telephone, MobileTelephone, Whatsup No, Telegram No, Email, WebSiteAddress
        Iterator<Row> it = sheet.rowIterator();
        if (!it.hasNext()) return; // header
    it.next(); // skip header

        Services currentParentService = null; // corresponds to last non-empty CATEGORY row's created/located service
        ServiceCategory currentCategory = null;
        if (defaultCategoryIdIfNone != null) {
            currentCategory = categoryRepo.findById(defaultCategoryIdIfNone).orElse(null);
        }

        while (it.hasNext()) {
            Row row = it.next();
            String categoryText = readString(row, 0);
            String subcategoryText = readString(row, 1);
            String companyName = readString(row, 2);
            // You provided company contact columns but didn't ask to persist them yet; ignore for now

            // Rule:
            // - If CATEGORY has text -> create/find a top-level service under asked categoryId (or defaultCategoryIdIfNone).
            //   Fill its attributes (we'll store name in ServiceTranslation EN; other columns ignored here).
            //   Then, if SUBCATAGORY present and sheetHasSubcategory=true, create sub-service under this parent.
            // - If CATEGORY empty -> use previous parent service; create a sub-service with SUBCATAGORY text (if any) or companyName as name.

            if (!isBlank(categoryText)) {
                // Ensure a category exists to attach services to
                if (currentCategory == null) {
                    throw new IllegalArgumentException("Category id not provided and not previously set for sheet '" + sheet.getSheetName() + "'");
                }
                Services parent = findOrCreateTopLevelServiceByName(categoryText, currentCategory, result);
                currentParentService = parent;

                if (sheetHasSubcategory && !isBlank(subcategoryText)) {
                    findOrCreateChildServiceByName(subcategoryText, currentCategory, parent, result);
                }
            } else {
                // No category text: rely on previous parent
                if (currentParentService == null) {
                    // If parent not yet set, try to fallback to defaultCategoryId and companyName as parent
                    if (currentCategory == null) {
                        if (defaultCategoryIdIfNone == null) {
                            // Nothing we can do for this row
                            result.messages.add("Row " + row.getRowNum() + ": no CATEGORY and no defaultCategoryId");
                            continue;
                        }
                        currentCategory = categoryRepo.findById(defaultCategoryIdIfNone).orElse(null);
                        if (currentCategory == null) {
                            result.messages.add("Default category id " + defaultCategoryIdIfNone + " not found");
                            continue;
                        }
                    }
                }
                String childName = !isBlank(subcategoryText) ? subcategoryText : (!isBlank(companyName) ? companyName : null);
                if (childName == null) {
                    // Nothing to create on this row
                    continue;
                }
                if (currentParentService == null) {
                    // Create an implicit parent using the first non-empty childName
                    currentParentService = findOrCreateTopLevelServiceByName(childName, currentCategory, result);
                } else {
                    findOrCreateChildServiceByName(childName, currentCategory, currentParentService, result);
                }
            }
        }
    }

    private Services findOrCreateTopLevelServiceByName(String name, ServiceCategory category, ImportResult result) {
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
        result.servicesCreated++;
        return service;
    }

    private Services findOrCreateChildServiceByName(String name, ServiceCategory category, Services parent, ImportResult result) {
        Optional<ServiceTranslation> existing = translationRepo
            .findFirstByNameIgnoreCaseAndService_Category_IdAndService_ServiceId(name, category.getId(), parent.getId());
        if (existing.isPresent()) {
            return existing.get().getService();
        }
        Services child = new Services();
        child.setCategory(category);
        child.setServiceId(parent.getId()); // set parent id linkage
        child = servicesRepo.save(child);
        ServiceTranslation t = new ServiceTranslation();
        t.setName(name);
        t.setService(child);
        translationRepo.save(t);
        // also add to parent's children set if loaded (optional)
        result.servicesCreated++;
        return child;
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
}
