package com.home.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.services.CombinedExcelImportService;
import com.home.service.services.CombinedExcelImportService.CombinedImportResult;

@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/import")
public class ImportController {

    private final CombinedExcelImportService combinedExcelImportService;
    private final AdminController adminController;

    public ImportController(CombinedExcelImportService combinedExcelImportService, AdminController adminController) {
        this.combinedExcelImportService = combinedExcelImportService;
        this.adminController = adminController;
    }

    // Single endpoint: POST /import/excel
    // form-data: file (xlsx or csv), defaultCategoryId (ServiceCategory to attach services), commonPassword (for created users)
    // Optional (xlsx): sheetIndex (single), sheetIndices (comma-separated, e.g. 0,1), processAllSheets=true
    @PostMapping("/excel")
    public ResponseEntity<CombinedImportResult> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("defaultCategoryId") Long defaultCategoryId,
            @RequestParam("commonPassword") String commonPassword,
            @RequestParam(value = "sheetIndex", required = false) Integer sheetIndex,
            @RequestParam(value = "sheetIndices", required = false) String sheetIndices,
            @RequestParam(value = "processAllSheets", required = false) Boolean processAllSheets
    ) {
        CombinedImportResult res = combinedExcelImportService.importAll(file, defaultCategoryId, commonPassword, sheetIndex, sheetIndices, processAllSheets);
        // Invalidate cached services/categories so the new imports appear immediately
        try {
            adminController.invalidateServicesCache();
        } catch (Exception ignore) { }
        return ResponseEntity.ok(res);
    }
}
