package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.document.SessionDocument;
import com.planet0088.universalCommunications.document.SessionMessage;
import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.repository.SessionRepository;
import com.planet0088.universalCommunications.service.SessionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Upsert pattern:
     *   - If session exists → add user turn message and save
     *   - If session is new → create document, add user turn, save
     */
    @Override
    public Mono<Void> recordUserTurn(CommunicateRequest request) {
        return sessionRepository.findById(request.sessionId())
                .defaultIfEmpty(SessionDocument.create(request.sessionId()))
                .flatMap(session -> {
                    session.addMessage(
                            SessionMessage.userTurn(
                                    request.payload(),
                                    request.inputType(),
                                    request.outputTypes()
                            )
                    );
                    return sessionRepository.save(session);
                })
                .then();             // Mono<SessionDocument> → Mono<Void>
    }

    /**
     * Appends the fully assembled assistant response to the session.
     * Called after the stream completes via doOnComplete in the translator.
     */
    @Override
    public Mono<Void> recordAssistantTurn(String sessionId, String fullResponse,
                                          CommunicateRequest request, long latencyMs) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    session.addMessage(
                            SessionMessage.assistantTurn(
                                    fullResponse,
                                    request.outputTypes(),
                                    latencyMs
                            )
                    );
                    return sessionRepository.save(session);
                })
                .then()
                .onErrorResume(e -> {
                    // Never let a session write failure break the stream response
                    System.err.printf("[SessionService] Failed to record assistant turn for session %s: %s%n",
                            sessionId, e.getMessage());
                    return Mono.empty();
                });
    }
}
