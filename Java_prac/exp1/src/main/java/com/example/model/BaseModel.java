package com.example.model;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class BaseModel {

    /**
     * Read CSV data from a resource file.
     * @param file the CSV file path (relative to classpath, e.g. "data/users.csv")
     * @return a map containing the CSV data: <column name, column values[]>
     */
    public Map<String, List<String>> readFile(String file) {
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
            return dataMap;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * Write CSV data to a file.
     * The file will be created or overwritten at the specified path.
     * @param file the file path to write the CSV data to (e.g. "output/data.csv")
     * @param dataMap a map containing the CSV data: <column name, column values[]>
     */
    public boolean writeFile(String file, Map<String, List<String>> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            // 如果数据为空，可以选择创建一个空文件或直接返回
            System.out.println(
                "Warning: Data map is null or empty, creating an empty file (or doing nothing)."
            );
            // 考虑在此处创建一个空文件，但为简洁，此处选择返回
            return true;
        }

        // 获取列名（表头）
        String[] headers = dataMap.keySet().toArray(new String[0]);
        if (headers.length == 0) {
            System.out.println(
                "Warning: Data map has no headers, file will be empty or contain only an empty line."
            );
            return true;
        }

        // 确定数据的行数 (假设所有列表的长度相同，以第一个列表的长度为准)
        int rowCount = 0;
        if (headers.length > 0) {
            rowCount = dataMap.get(headers[0]).size();
        }

        // 使用 try-with-resources 确保 CSVWriter 和 FileWriter 被正确关闭
        try (
            // 使用 FileWriter 写入文件，true 表示追加，但通常写入 CSV 是覆盖，所以默认是 false
            CSVWriter writer = new CSVWriter(new FileWriter(file))
        ) {
            // 1. 写入表头
            writer.writeNext(headers);

            // 2. 写入数据行
            // 循环行数
            for (int i = 0; i < rowCount; i++) {
                String[] rowData = new String[headers.length];
                // 循环列数，从 dataMap 中取出对应的值
                for (int j = 0; j < headers.length; j++) {
                    String header = headers[j];
                    List<String> columnData = dataMap.get(header);

                    // 确保不会因为数据不规则（列表长度不一致）而越界
                    if (i < columnData.size()) {
                        rowData[j] = columnData.get(i);
                    } else {
                        // 如果当前列的数据行数不足，用空字符串填充
                        rowData[j] = null;
                    }
                }
                writer.writeNext(rowData);
            }

            // 确保所有数据都写入文件
            writer.flush();
            return true;
        } catch (IOException e) {
            // 打印异常信息，通知用户写入失败
            System.err.println("Error writing CSV file: " + file);
            e.printStackTrace();
            return false;
        }
    }
}
