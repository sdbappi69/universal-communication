package com.planet0088.universalCommunications.document;

import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.model.enums.OutputType;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

/**
 * Embedded document — stored inside SessionDocument.messages[].
 * Represents one complete request/response turn in a session.
 *
 * NOT a top-level @Document — never persisted standalone.
 */
@Data
public class SessionMessage {

    @Field("role")
    private String role;                    // "user" or "assistant"

    @Field("input_type")
    private InputType inputType;

    @Field("output_types")
    private List<OutputType> outputTypes;

    @Field("raw_payload")
    private String rawPayload;              // what the user sent

    @Field("full_response")
    private String fullResponse;            // assembled from all chunks

    @Field("timestamp")
    private Instant timestamp;

    @Field("latency_ms")
    private Long latencyMs;

    // ── static factories ──────────────────────────────────────────────────────

    public static SessionMessage userTurn(String payload, InputType inputType, List<OutputType> outputTypes) {
        SessionMessage msg = new SessionMessage();
        msg.role = "user";
        msg.rawPayload = payload;
        msg.inputType = inputType;
        msg.outputTypes = outputTypes;
        msg.timestamp = Instant.now();
        return msg;
    }

    public static SessionMessage assistantTurn(String fullResponse, List<OutputType> outputTypes, long latencyMs) {
        SessionMessage msg = new SessionMessage();
        msg.role = "assistant";
        msg.fullResponse = fullResponse;
        msg.outputTypes = outputTypes;
        msg.latencyMs = latencyMs;
        msg.timestamp = Instant.now();
        return msg;
    }

}
