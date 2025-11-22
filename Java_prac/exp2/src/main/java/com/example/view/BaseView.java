package com.example.view;

import com.example.model.dto.BinaryMessage;
import java.util.Scanner;

public abstract class BaseView {

    protected static final Scanner scanner = new Scanner(System.in);
    private String sessionId;

    /**
     * 供子类调用用户输入
     */
    protected String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    protected String readLine() {
        return scanner.nextLine();
    }

    protected void close() {
        scanner.close();
    }

    public void show(BinaryMessage msg) {
        if (msg.isBool_result()) {
            System.out.println("❌ " + msg.getMessage());
        } else {
            System.out.println("✅ " + msg.getMessage());
        }
    }

    public void show(String msg) {
        System.out.println(msg);
    }

    /**
     * 设置会话ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取会话ID
     */
    public String getSessionId() {
        return this.sessionId;
    }
}
