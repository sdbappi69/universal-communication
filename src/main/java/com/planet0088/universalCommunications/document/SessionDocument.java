package com.planet0088.universalCommunications.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Document(collection = "sessions")
public class SessionDocument {

    @Id
    private String sessionId;

    @Field("created_at")
    private Instant createdAt;

    @Field("last_active_at")
    @Indexed(expireAfterSeconds = 86400)
    private Instant lastActiveAt;

    public static SessionDocument create(String sessionId) {
        SessionDocument doc = new SessionDocument();
        doc.sessionId = sessionId;
        doc.createdAt = Instant.now();
        doc.lastActiveAt = Instant.now();
        return doc;
    }
}
