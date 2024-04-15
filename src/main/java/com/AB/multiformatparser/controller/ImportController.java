package com.AB.multiformatparser.controller;

import com.AB.multiformatparser.service.ImportService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/upload")
public class ImportController {
    private final ImportService importService;
    public ImportController(ImportService importService) {
        this.importService = importService;
    }
    @GetMapping
    public String showUploadForm() {
        return "Form";
    }
    @PostMapping("/json")
    public ResponseEntity<byte[]> uploadJsonFile(@RequestParam("jsonFile") MultipartFile file) {
        if (!isFileTypeValid(file, "json")) {
            String errorMessage = "Error: Invalid file type. Only JSON files are allowed.";
            return ResponseEntity.status(400).body(errorMessage.getBytes());
        }
        try {
           ResponseEntity<byte[]> response = importService.processData(file);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    @PostMapping("/xlsx")
    public ResponseEntity<String> uploadXlsxFile(@RequestParam("xlsxFile") MultipartFile file) {
        if (!isFileTypeValid(file, "xlsx")) {
            return ResponseEntity.status(400).body("Error: Invalid file type. Only XLSX files are allowed.");
        }
    ResponseEntity<String> response = importService.convertExcelToJson(file);
    return  response;
    }
    private boolean isFileTypeValid(MultipartFile file, String expectedType) {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        return fileExtension.equals(expectedType);
    }
}
