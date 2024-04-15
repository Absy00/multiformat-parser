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
    private final long  MAX_FILE_SIZE = 10485760;
    public ImportController(ImportService importService) {
        this.importService = importService;
    }
    @GetMapping
    public String showUploadForm() {
        return "Form";
    }
    @PostMapping("/json")
    public ResponseEntity<byte[]> uploadJsonFile(@RequestParam("jsonFile")  MultipartFile file) {
        if (!isFileTypeValid(file, "json")) {
            String errorMessage = "Error: Invalid file type. Only JSON files are allowed.";
            return ResponseEntity.status(400).body(errorMessage.getBytes());
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            String errorSizeMessage = "Error: File size exceeds the maximum limit of " + MAX_FILE_SIZE / (1024 * 1024) + " MB. ";
            return ResponseEntity.status(413).body(errorSizeMessage.getBytes());
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
        if (file.getSize() > MAX_FILE_SIZE) {
            String errorSizeMessage = "Error: File size exceeds the maximum limit of " + MAX_FILE_SIZE / (1024 * 1024) + " MB. ";
            return ResponseEntity.status(413).body(errorSizeMessage);
        }
    ResponseEntity<String> response = importService.convertExcelToJson(file);
    return  response;
    }
    @PostMapping("/xml")
    public ResponseEntity<String> uploadXmlFile(@RequestParam("xmlFile") MultipartFile file) {
        if (!isFileTypeValid(file, "xml")) {
            return ResponseEntity.status(400).body("Error: Invalid file type. Only XML files are allowed.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            String errorSizeMessage = "Error: File size exceeds the maximum limit of " + MAX_FILE_SIZE / (1024 * 1024) + " MB. ";
            return ResponseEntity.status(413).body(errorSizeMessage);
        }
        ResponseEntity<String> response = importService.convertXmlToJson(file);
                return response;
    }
    private boolean isFileTypeValid(MultipartFile file, String expectedType) {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        return fileExtension.equals(expectedType);
    }
}
