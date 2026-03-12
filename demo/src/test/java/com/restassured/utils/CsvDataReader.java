package com.restassured.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CsvDataReader - Reads test data from CSV files.
 * No extra dependencies needed — uses built-in Java.
 */
public class CsvDataReader {

    /**
     * Reads all rows from a CSV file.
     * First row is treated as headers.
     *
     * @param filePath path to .csv file
     * @return List of Maps, each Map representing one row
     */
    public static List<Map<String, String>> readCsv(String filePath) {
        List<Map<String, String>> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read headers
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new RuntimeException("❌ CSV file is empty: " + filePath);
            }
            String[] headers = headerLine.split(",");

            // Read data rows
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",", -1); // -1 keeps empty trailing values
                Map<String, String> rowData = new HashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    String value = i < values.length ? values[i].trim() : "";
                    rowData.put(headers[i].trim(), value);
                }
                data.add(rowData);
            }

            System.out.println("✅ Read " + data.size() + " rows from CSV: " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to read CSV file: " + filePath, e);
        }

        return data;
    }

    /**
     * Reads CSV data as a 2D Object array for TestNG @DataProvider.
     */
    public static Object[][] readCsvAsDataProvider(String filePath) {
        List<Map<String, String>> data = readCsv(filePath);
        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i);
        }
        return result;
    }
}
