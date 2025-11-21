package com.example.session;

import java.util.List;
import java.util.UUID;

public class SessionManager {

    private static List<Session> currentSessionPool = null;

    public static String addSession(
        String userId,
        String username,
        String role,
        java.util.Set<String> permissions
    ) {
        Session session = new Session(
            UUID.randomUUID().toString(),
            userId,
            username,
            role,
            permissions
        );
        currentSessionPool.add(session);
        return session.getSessionId();
    }

    public static Session getSession(String sessionId) {
        return currentSession;
    }

    public static void clear() {
        currentSession = null;
    }
}
