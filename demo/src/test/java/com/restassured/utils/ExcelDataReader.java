package com.restassured.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExcelDataReader - Reads test data from Excel (.xlsx) files.
 * Returns each row as a Map of column header → cell value.
 */
public class ExcelDataReader {

    /**
     * Reads all rows from an Excel sheet.
     * First row is treated as headers.
     *
     * @param filePath  path to .xlsx file
     * @param sheetName name of the sheet to read
     * @return List of Maps, each Map representing one row
     */
    public static List<Map<String, String>> readExcel(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("❌ Sheet '" + sheetName + "' not found in: " + filePath);
            }

            // Read headers from first row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell));
            }

            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), cell != null ? getCellValue(cell) : "");
                }
                data.add(rowData);
            }

            System.out.println("✅ Read " + data.size() + " rows from Excel: " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to read Excel file: " + filePath, e);
        }

        return data;
    }

    /**
     * Reads Excel data as a 2D Object array for TestNG @DataProvider.
     */
    public static Object[][] readExcelAsDataProvider(String filePath, String sheetName) {
        List<Map<String, String>> data = readExcel(filePath, sheetName);
        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i);
        }
        return result;
    }

    /**
     * Gets cell value as String regardless of cell type.
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                            ? cell.getDateCellValue().toString()
                            : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default      -> "";
        };
    }
}
