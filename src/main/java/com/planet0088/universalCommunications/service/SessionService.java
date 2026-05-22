package com.planet0088.universalCommunications.service;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import reactor.core.publisher.Mono;

public interface SessionService {

    /**
     * Called once per request BEFORE the stream starts.
     * Upserts the session document and appends the user turn message.
     */
    Mono<Void> recordUserTurn(CommunicateRequest request);

    /**
     * Called once per request AFTER the stream completes.
     * Appends the fully assembled assistant response and latency to the session.
     *
     * @param sessionId      the session to update
     * @param fullResponse   all chunks joined into one string
     * @param request        original request (for outputTypes)
     * @param latencyMs      total time from first token to last token
     */
    Mono<Void> recordAssistantTurn(String sessionId, String fullResponse,
                                   CommunicateRequest request, long latencyMs);
}
