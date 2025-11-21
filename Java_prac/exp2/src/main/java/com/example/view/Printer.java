package com.example.view;

import com.example.model.message_type.BinaryMessage;

/**
 * 视图层，负责向屏幕上打印内容
 */
class Printer {

    void show(String message) {
        System.out.println(message);
    }

    void show(BinaryMessage msg) {
        if (msg.isBool_result()) {
            System.out.println("❌ " + msg.getMessage());
        } else {
            System.out.println("✅ " + msg.getMessage());
        }
    }
}
