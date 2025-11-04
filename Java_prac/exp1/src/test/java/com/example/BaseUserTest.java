package com.example;

import static org.junit.jupiter.api.Assertions.*;

import com.example.model.user.BaseUser;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseUserTest {

    private BaseUser baseUser;

    @BeforeEach
    void setUp() {
        baseUser = new BaseUser();
    }

    @Test
    @DisplayName("测试读取文件内容")
    void readFile_Test() {
        Map<String, String[]> data = baseUser.readFile("data/test.cvs");

        // 1️⃣ 验证读取结果不为空
        assertNotNull(data, "返回的数据 Map 不应为 null");
        assertFalse(data.isEmpty(), "数据不应为空，请检查文件路径或内容");

        // 2️⃣ 输出读取到的内容，方便调试
        System.out.println("\n读取到的文件内容：");
        data.forEach((key, value) ->
            System.out.println(key + " -> " + Arrays.toString(value))
        );

        // 3️⃣ 验证文件中应包含某些列名
        assertTrue(data.containsKey("id"), "文件中应包含列 'id'");
        assertTrue(data.containsKey("name"), "文件中应包含列 'name'");
        assertTrue(data.containsKey("gender"), "文件中应包含列 'gender'");
        assertTrue(data.containsKey("age"), "文件中应包含列 'age'");
        assertTrue(data.containsKey("password"), "文件中应包含列 'password'");

        // 4️⃣ 验证每一列的数据长度一致
        int expectedSize = data.values().iterator().next().length;
        for (String[] columnValues : data.values()) {
            assertEquals(
                expectedSize,
                columnValues.length,
                "每列数据长度应一致"
            );
        }

        // ✅ 可选：验证具体数据值（如果已知）
        String[] names = data.get("name");
        assertNotNull(names);
        assertTrue(names.length > 0, "应至少有一条学生数据");
    }
}
