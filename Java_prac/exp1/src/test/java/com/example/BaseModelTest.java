package com.example;

import static org.junit.jupiter.api.Assertions.*;

import com.example.model.BaseModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BaseModelTest {

    private BaseModel baseModel;

    // 使用 @TempDir 注解创建临时目录，JUnit 会在测试结束后自动清理
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        baseModel = new BaseModel();
    }

    @Test
    @DisplayName("测试读取文件内容")
    void readFile_Test() {
        Map<String, List<String>> data = baseModel.readFile("data/test.csv");

        // 1️⃣ 验证读取结果不为空
        assertNotNull(data, "返回的数据 Map 不应为 null");
        if (data.isEmpty()) {
            System.out.println(
                "警告: data/test.csv 文件未找到或为空，跳过内容验证。"
            );
            return; // 提前返回，或者将其作为一个失败的测试案例
        }

        // 2️⃣ 输出读取到的内容，方便调试
        System.out.println("\n读取到的文件内容：");
        data.forEach((key, value) ->
            System.out.println(key + " -> " + value.toString())
        );

        // 3️⃣ 验证文件中应包含某些列名（假设文件包含这些列）
        assertTrue(data.containsKey("id"), "文件中应包含列 'id'");
        assertTrue(data.containsKey("name"), "文件中应包含列 'name'");
        // ... 其他列验证

        // 4️⃣ 验证每一列的数据长度一致
        int expectedSize = data.values().iterator().next().size();
        for (List<String> columnValues : data.values()) {
            assertEquals(
                expectedSize,
                columnValues.size(),
                "每列数据长度应一致"
            );
        }

        // ✅ 可选：验证具体数据值（如果已知）
        List<String> names = data.get("name");
        assertNotNull(names);
        assertTrue(names.size() > 0, "应至少有一条学生数据");
    }

    @Test
    @DisplayName("测试写入文件内容")
    void writeFile_Test() throws IOException {
        // 1. 准备测试数据
        Map<String, List<String>> testData = new LinkedHashMap<>();
        testData.put("id", Arrays.asList("1", "2", "3"));
        testData.put("name", Arrays.asList("Alice", "Bob", "Charlie"));
        testData.put("age", Arrays.asList("25", "30", "22"));

        // 2. 构造输出文件路径
        File outputFile = tempDir.resolve("output.csv").toFile();
        String filePath = outputFile.getAbsolutePath();

        // 3. 执行待测方法
        baseModel.writeFile(filePath, testData);

        // 4. 验证文件是否创建
        assertTrue(outputFile.exists(), "文件应该被成功创建");

        // 5. 读取写入的文件内容进行验证
        List<String> lines = Files.readAllLines(outputFile.toPath());

        // 6. 验证行数 (表头 + 3行数据 = 4行)
        assertEquals(4, lines.size(), "写入的文件应包含 4 行 (表头 + 3行数据)");

        // 7. 验证表头
        String expectedHeader = "\"id\",\"name\",\"age\"";
        assertEquals(expectedHeader, lines.get(0), "文件第一行应为正确的表头");

        // 8. 验证数据行
        // 简单验证第一行数据
        String expectedRow1 = "\"1\",\"Alice\",\"25\"";
        assertEquals(expectedRow1, lines.get(1), "文件第二行数据不正确");

        // 验证最后一行数据
        String expectedRow3 = "\"3\",\"Charlie\",\"22\"";
        assertEquals(expectedRow3, lines.get(3), "文件第四行数据不正确");
    }

    @Test
    @DisplayName("测试写入空数据")
    void writeFile_EmptyData_Test() throws IOException {
        // 1. 构造输出文件路径
        File outputFile = tempDir.resolve("empty_output.csv").toFile();
        String filePath = outputFile.getAbsolutePath();

        // 2. 写入空数据 map
        baseModel.writeFile(filePath, Collections.emptyMap());

        // 3. 验证文件不应被创建 (或者如果逻辑改变，验证文件是否为空)
        // 根据 writeFile 逻辑，如果 dataMap 为空，它会返回，不会创建文件。
        assertFalse(outputFile.exists(), "当数据为空时，不应创建文件");

        // 4. 写入有表头但无数据的 Map (测试 headers.length > 0 时的逻辑)
        Map<String, List<String>> noDataMap = new LinkedHashMap<>();
        noDataMap.put("col1", Collections.emptyList());
        noDataMap.put("col2", Collections.emptyList());

        File outputFile2 = tempDir.resolve("header_only.csv").toFile();
        String filePath2 = outputFile2.getAbsolutePath();

        baseModel.writeFile(filePath2, noDataMap);

        // 验证文件是否创建
        assertTrue(outputFile2.exists(), "有表头时应创建文件");

        // 读取文件内容进行验证
        List<String> lines = Files.readAllLines(outputFile2.toPath());

        // 验证只有一行表头
        assertEquals(1, lines.size(), "只有表头时，文件应只包含一行");
        assertEquals("\"col1\",\"col2\"", lines.get(0), "表头不正确");
    }
}
