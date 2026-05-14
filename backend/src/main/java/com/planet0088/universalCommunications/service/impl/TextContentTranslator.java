package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.service.ContentTranslator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sprint 1 implementation: TEXT input → TEXT output only.
 *
 * Stubs for other input/output types will be added in subsequent sprints.
 * Unsupported input types fall through to the text pipeline for now,
 * using the raw payload as-is — this keeps the SSE pipeline testable
 * end-to-end without blocking Sprint 1 on preprocessor work.
 */
@Service
public class TextContentTranslator implements ContentTranslator {

    private final ChatClient chatClient;

    public TextContentTranslator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are an accessible communication assistant.
                        Respond clearly and concisely. Adapt your language
                        to be as inclusive and easy to understand as possible.
                        """)
                .build();
    }

    @Override
    public Flux<ContentChunk> translate(CommunicateRequest request) {
        // seq counter per request — not per session, no shared state needed here
        AtomicInteger seq = new AtomicInteger(0);

        return chatClient
                .prompt()
                .user(request.payload())
                .stream()
                .content()                              // Flux<String> — one token per element
                .filter(token -> !token.isBlank())      // drop whitespace-only tokens
                .map(token -> new ContentChunk(
                        request.sessionId(),
                        OutputType.TEXT,
                        token,
                        seq.getAndIncrement()
                ))
                .doOnError(e -> {
                    // Log but don't swallow — let the controller's onErrorResume handle it
                    System.err.printf("[ContentTranslator] Stream error for session %s: %s%n",
                            request.sessionId(), e.getMessage());
                });
    }
}