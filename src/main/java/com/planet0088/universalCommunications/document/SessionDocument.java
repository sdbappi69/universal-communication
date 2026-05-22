package com.planet0088.universalCommunications.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level MongoDB document — one per session.
 * Collection name: "sessions"
 *
 * Messages are embedded as an array. This is intentional for Sprint 1-2
 * since you always load the full session context together.
 *
 * WARNING: MongoDB document limit is 16MB. Add overflow handling
 * (Sprint 4+) once messages array can grow unbounded in long sessions.
 */
@Document(collection = "sessions")
@Data
public class SessionDocument {

    @Id
    private String sessionId;               // same as the sessionId in the request

    @Field("created_at")
    private Instant createdAt;

    @Field("last_active_at")
    @Indexed(expireAfterSeconds = 86400)    // TTL: auto-delete sessions inactive for 24h
    private Instant lastActiveAt;

    @Field("messages")
    private List<SessionMessage> messages = new ArrayList<>();

    // ── static factory ────────────────────────────────────────────────────────

    public static SessionDocument create(String sessionId) {
        SessionDocument doc = new SessionDocument();
        doc.sessionId = sessionId;
        doc.createdAt = Instant.now();
        doc.lastActiveAt = Instant.now();
        return doc;
    }

    public void addMessage(SessionMessage message) {
        this.messages.add(message);
        this.lastActiveAt = Instant.now();
    }
}
