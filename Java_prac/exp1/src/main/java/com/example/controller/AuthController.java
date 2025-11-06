package com.example.controller;

import com.example.model.user.Student;
import com.example.model.user.Teacher;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthController {

    private String sessionToken = null;
    private final Student studentData = new Student();
    private final Teacher teacherData = new Teacher();

    public AuthController() {}

    public Map<String, String> handleLogin(String userid, String password) {
        Map<String, String> result = new HashMap<>();
        String role = getUserRole(userid);
        if (role.equals("unknown")) {
            result.put("res", "false");
            result.put("reason", "用户不存在");
            return result;
        } else {
            result.put("role", role);
        }
        Integer index = getUserIndex(userid);
        if (validatePassword(role, index, password)) {
            UUID token = UUID.randomUUID();
            sessionToken = token.toString();
            result.put("res", "true");
            result.put("reason", "登录成功");
            result.put("userid", userid);
            result.put("token", token.toString());
            return result;
        } else {
            result.put("res", "false");
            result.put("reason", "密码错误");
            return result;
        }
    }

    public Integer getUserIndex(String userid) {
        Integer index = studentData.getIndxexById(userid);
        if (index != -1) {
            return index;
        }
        index = teacherData.getIndxexById(userid);
        if (index != -1) {
            return index;
        }
        return index;
    }

    public String getUserRole(String userid) {
        if (studentData.getIndxexById(userid) != -1) {
            return "student";
        }
        if (teacherData.getIndxexById(userid) != -1) {
            return "teacher";
        }
        return "unknown";
    }

    private boolean validatePassword(
        String role,
        Integer index,
        String password
    ) {
        switch (role) {
            case "student":
                return studentData.getPasswordByIndex(index).equals(password);
            case "teacher":
                return teacherData.getPasswordByIndex(index).equals(password);
            default:
                return false;
        }
    }

    public boolean checkToken(String _token) {
        if (sessionToken == null || _token == null) {
            return false;
        } else {
            return sessionToken.equals(_token);
        }
    }
}
