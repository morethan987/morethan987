package com.example.model.user;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class BaseUser {

    /**
     * Read CSV data from a resource file.
     * @param file the CSV file path (relative to classpath, e.g. "data/users.csv")
     * @return a map containing the CSV data: <column name, column values[]>
     */
    public Map<String, String[]> readFile(String file) {
        Map<String, List<String>> dataMap = new LinkedHashMap<>();

        try (
            InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(file);
            InputStreamReader streamReader = inputStream != null
                ? new InputStreamReader(inputStream)
                : null;
            CSVReader csvReader = streamReader != null
                ? new CSVReader(streamReader)
                : null
        ) {
            if (
                inputStream == null || streamReader == null || csvReader == null
            ) {
                throw new IllegalArgumentException(
                    "Resource not found: " + file
                );
            }

            // 读取所有行
            List<String[]> rows = csvReader.readAll();
            if (rows.isEmpty()) {
                return Collections.emptyMap();
            }

            // 第一行是表头
            String[] headers = rows.get(0);
            for (String header : headers) {
                dataMap.put(header, new ArrayList<>());
            }

            // 读取数据行
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                for (int j = 0; j < headers.length && j < row.length; j++) {
                    dataMap.get(headers[j]).add(row[j]);
                }
            }

            // 转换为 <String, String[]>
            Map<String, String[]> result = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : dataMap.entrySet()) {
                result.put(
                    entry.getKey(),
                    entry.getValue().toArray(new String[0])
                );
            }

            return result;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
