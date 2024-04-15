package com.AB.multiformatparser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

@Service
public class ImportService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<byte[]> processData(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Object jsonData;
            try {
                jsonData = new ObjectMapper().readValue(is, Object.class);
            } catch (JsonParseException e) {
                return ResponseEntity.badRequest()
                        .body("Error: Invalid JSON format in uploaded file.".getBytes());
            }
            if (!(jsonData instanceof Map) && !(jsonData instanceof List)) {
                return ResponseEntity.badRequest()
                        .body("Error: Only JSON objects or arrays are supported.".getBytes());
            }
            Map<String, Object> flattenedData = processJsonData(jsonData);
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Data");
            Row headerRow = sheet.createRow(0);
            int cellNum = 0;
            for (String key : flattenedData.keySet()) {
                Cell cell = headerRow.createCell(cellNum++);
                cell.setCellValue(key);
            }
            Row valueRow = sheet.createRow(1);
            cellNum = 0;
            for (Object value : flattenedData.values()) {
                Cell cell = valueRow.createCell(cellNum++);
                setCellValueByType(cell, value);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(null);
            } finally {
                try {
                    workbook.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "Data_" + formatter.format(new Date()) + ".xlsx";
            return ResponseEntity
                    .ok()
                    .header("Content-Disposition", "attachment; filename=" + fileName)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    private Map<String, Object> processJsonData(Object jsonData) throws JsonProcessingException {
        if (jsonData instanceof Map) {
            return JsonFlattener.flattenAsMap(new ObjectMapper().writeValueAsString(jsonData));
        } else {
            return new HashMap<>();
        }
    }

    private void setCellValueByType(Cell cell, Object value) {
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else {
            cell.setCellValue("Unsupported data type");
        }
    }

    public ResponseEntity<String>   convertExcelToJson(MultipartFile    file) {
        try {
            InputStream is = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            Row headerRow = rows.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }
            List<ObjectNode> jsonObjects = new ArrayList<>();
            while (rows.hasNext()) {
                Row row = rows.next();
                ObjectNode jsonObject = objectMapper.createObjectNode();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    String cellValue = cell == null ? "" : cell.toString();
                    jsonObject.put(headers.get(i), cellValue);
                }
                jsonObjects.add(jsonObject);
            }
            String jsonOutput = objectMapper.writeValueAsString(jsonObjects);
            workbook.close();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "output_" + formatter.format(new Date()) + ".json";
            return ResponseEntity
                    .ok()
                    .header("Content-Disposition", "attachment;    filename=" + fileName)
                    .body(jsonOutput);
        }   catch (Exception    e)  {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

}
