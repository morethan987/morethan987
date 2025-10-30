package com.example;

import static org.junit.jupiter.api.Assertions.*;

import com.example.controller.SystemController;
import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void testAdd() {
        SystemController sc = new SystemController();
        assertEquals(5, sc.add(2, 3));
    }
}
