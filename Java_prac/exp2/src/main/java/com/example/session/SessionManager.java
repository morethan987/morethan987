package com.example.session;

import com.example.model.entity.Role;
import com.example.util.LoggerUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionManager {

    private static List<Session> currentSessionPool = new ArrayList<>();

    public static String addSession(
        String userId,
        String username,
        List<Role> roleList
    ) {
        LoggerUtil.debug(
            "创建新会话 - UserID: %s, Username: %s, Roles: %s",
            userId,
            username,
            roleList.stream().map(Role::getName).toArray()
        );

        Session session = new Session(
            UUID.randomUUID().toString(),
            userId,
            username,
            roleList
        );
        currentSessionPool.add(session);

        LoggerUtil.info(
            "会话创建成功 - SessionID: %s, Username: %s",
            session.getSessionId(),
            username
        );
        LoggerUtil.debug("当前活跃会话数: " + currentSessionPool.size());

        return session.getSessionId();
    }

    public static Session getSession(String sessionId) {
        LoggerUtil.debug("查找会话 - SessionID: %s", sessionId);

        for (Session session : currentSessionPool) {
            if (session.getSessionId().equals(sessionId)) {
                LoggerUtil.debug(
                    "会话找到 - SessionID: %s, Username: %s",
                    sessionId,
                    session.getUsername()
                );
                return session;
            }
        }

        LoggerUtil.debug("会话未找到 - SessionID: %s", sessionId);
        return null;
    }

    public static List<Role> getRoleBySessionId(String sessionId) {
        LoggerUtil.debug("获取会话角色 - SessionID: %s", sessionId);

        Session session = getSession(sessionId);
        if (session != null) {
            List<Role> roles = session.getRoleSet();
            LoggerUtil.debug(
                "获取会话角色成功 - SessionID: %s, Roles: %s",
                sessionId,
                roles.stream().map(Role::getName).toArray()
            );
            return roles;
        }

        LoggerUtil.debug(
            "获取会话角色失败，会话不存在 - SessionID: %s",
            sessionId
        );
        return null;
    }

    public static void clear(String sessionId) {
        LoggerUtil.info("清理会话 - SessionID: %s", sessionId);

        boolean removed = false;
        for (int i = 0; i < currentSessionPool.size(); i++) {
            Session session = currentSessionPool.get(i);
            if (session.getSessionId().equals(sessionId)) {
                LoggerUtil.debug(
                    "移除会话 - SessionID: %s, Username: %s",
                    sessionId,
                    session.getUsername()
                );
                currentSessionPool.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            LoggerUtil.info("会话清理成功 - SessionID: %s", sessionId);
        } else {
            LoggerUtil.warning(
                "会话清理失败，会话不存在 - SessionID: %s",
                sessionId
            );
        }

        LoggerUtil.debug("当前活跃会话数: " + currentSessionPool.size());
    }
}
