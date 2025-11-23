package com.example.session;

import com.example.model.entity.Role;
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
        Session session = new Session(
            UUID.randomUUID().toString(),
            userId,
            username,
            roleList
        );
        currentSessionPool.add(session);
        return session.getSessionId();
    }

    public static Session getSession(String sessionId) {
        for (Session session : currentSessionPool) {
            if (session.getSessionId().equals(sessionId)) {
                return session;
            }
        }
        return null;
    }

    public static List<Role> getRoleBySessionId(String sessionId) {
        Session session = getSession(sessionId);
        if (session != null) {
            return session.getRoleSet();
        }
        return null;
    }

    public static void clear(String sessionId) {
        for (Session session : currentSessionPool) {
            if (session.getSessionId().equals(sessionId)) {
                session = null;
            }
        }
    }
}
