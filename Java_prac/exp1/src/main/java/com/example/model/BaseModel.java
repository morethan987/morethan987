package com.example.model;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.*;
import java.util.*;

public class BaseModel {

    // 数据存储目录（项目根目录下的 data 文件夹）
    private static final String DATA_DIR = "data" + File.separator;

    // 模板文件目录（resources 中的路径）
    private static final String TEMPLATE_DIR = "data/";

    /**
     * 确保数据目录存在
     */
    private void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.out.println(
                    "Created data directory failed: " + DATA_DIR
                );
            }
        }
    }

    /**
     * 获取外部数据文件的完整路径
     * @param file 文件名（如 "teacher.csv"）
     * @return 外部文件路径
     */
    private String getExternalFilePath(String file) {
        // 移除 "data/" 前缀（如果有）
        String fileName = file.startsWith(TEMPLATE_DIR)
            ? file.substring(TEMPLATE_DIR.length())
            : file;
        return DATA_DIR + fileName;
    }

    /**
     * 从模板文件初始化外部数据文件
     * @param templateFile 模板文件路径（相对于 resources）
     * @param externalFile 外部文件路径
     * @return 是否初始化成功
     */
    private boolean initializeFromTemplate(
        String templateFile,
        String externalFile
    ) {
        System.out.println(
            "Initializing data file from template: " + templateFile
        );

        try (
            InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(templateFile);
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
                System.err.println("Template file not found: " + templateFile);
                return false;
            }

            // 读取模板数据
            List<String[]> rows = csvReader.readAll();
            if (rows.isEmpty()) {
                System.err.println("Template file is empty: " + templateFile);
                return false;
            }

            // 写入外部文件
            try (
                CSVWriter writer = new CSVWriter(new FileWriter(externalFile))
            ) {
                writer.writeAll(rows);
                writer.flush();
                System.out.println("Successfully initialized: " + externalFile);
                return true;
            }
        } catch (IOException | CsvException e) {
            System.err.println(
                "Error initializing from template: " + templateFile
            );
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从外部文件读取 CSV 数据
     * @param filePath 外部文件路径
     * @return CSV 数据映射
     */
    private Map<String, List<String>> readFromExternalFile(String filePath) {
        Map<String, List<String>> dataMap = new LinkedHashMap<>();

        try (
            FileReader fileReader = new FileReader(filePath);
            CSVReader csvReader = new CSVReader(fileReader)
        ) {
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
            System.err.println("Error reading external file: " + filePath);
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * Read CSV data from a resource file.
     * 首次运行时，会从 resources 中的模板文件初始化外部数据文件。
     * 后续运行时，直接从外部数据文件读取。
     *
     * @param file the CSV file path (relative to classpath, e.g. "data/users.csv")
     * @return a map containing the CSV data: <column name, column values[]>
     */
    public Map<String, List<String>> readFile(String file) {
        ensureDataDirectory();

        String externalFilePath = getExternalFilePath(file);
        File externalFile = new File(externalFilePath);

        // 如果外部文件不存在，从模板初始化
        if (!externalFile.exists()) {
            System.out.println(
                "External data file not found, initializing from template..."
            );
            boolean initialized = initializeFromTemplate(
                file,
                externalFilePath
            );
            if (!initialized) {
                System.err.println("Failed to initialize data file: " + file);
                return Collections.emptyMap();
            }
        }

        // 从外部文件读取数据
        return readFromExternalFile(externalFilePath);
    }

    /**
     * Write CSV data to a file.
     * 数据将写入到项目根目录下的 data 文件夹中。
     * The file will be created or overwritten at the specified path.
     *
     * @param file the file path to write the CSV data to (e.g. "data/teacher.csv")
     * @param dataMap a map containing the CSV data: <column name, column values[]>
     * @return true if write successful, false otherwise
     */
    public boolean writeFile(String file, Map<String, List<String>> dataMap) {
        ensureDataDirectory();

        String externalFilePath = getExternalFilePath(file);

        if (dataMap == null || dataMap.isEmpty()) {
            System.out.println("Warning: Data map is null or empty.");
            return true;
        }

        // 获取列名（表头）
        String[] headers = dataMap.keySet().toArray(new String[0]);
        if (headers.length == 0) {
            System.out.println("Warning: Data map has no headers.");
            return true;
        }

        // 确定数据的行数
        int rowCount = 0;
        if (headers.length > 0) {
            rowCount = dataMap.get(headers[0]).size();
        }

        try (
            CSVWriter writer = new CSVWriter(new FileWriter(externalFilePath))
        ) {
            // 1. 写入表头
            writer.writeNext(headers);

            // 2. 写入数据行
            for (int i = 0; i < rowCount; i++) {
                String[] rowData = new String[headers.length];
                for (int j = 0; j < headers.length; j++) {
                    String header = headers[j];
                    List<String> columnData = dataMap.get(header);

                    // 确保不会因为数据不规则而越界
                    if (i < columnData.size()) {
                        rowData[j] = columnData.get(i);
                    } else {
                        rowData[j] = null;
                    }
                }
                writer.writeNext(rowData);
            }

            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + externalFilePath);
            e.printStackTrace();
            return false;
        }
    }
}
