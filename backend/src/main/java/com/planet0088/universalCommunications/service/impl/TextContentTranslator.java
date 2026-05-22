package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.service.ContentTranslator;
import com.planet0088.universalCommunications.service.SessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TextContentTranslator implements ContentTranslator {

    private final ChatClient chatClient;
    private final SessionService sessionService;

    public TextContentTranslator(ChatClient.Builder chatClientBuilder,
                                 SessionService sessionService) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are an accessible communication assistant.
                        Respond clearly and concisely. Adapt your language
                        to be as inclusive and easy to understand as possible.
                        """)
                .build();
        this.sessionService = sessionService;
    }

    @Override
    public Flux<ContentChunk> translate(CommunicateRequest request) {
        AtomicInteger seq = new AtomicInteger(0);
        StringBuilder responseAccumulator = new StringBuilder();
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

        return sessionService.recordUserTurn(request)    // write user turn first
                .thenMany(
                        chatClient
                                .prompt()
                                .user(request.payload())
                                .stream()
                                .content()
                                .filter(token -> !token.isBlank())
                                .map(token -> {
                                    responseAccumulator.append(token);  // accumulate for final write
                                    return new ContentChunk(
                                            request.sessionId(),
                                            OutputType.TEXT,
                                            token,
                                            seq.getAndIncrement()
                                    );
                                })
                                .doOnComplete(() ->
                                        // Fire-and-forget — subscribe() detaches from the main stream
                                        // so a slow Mongo write never delays the SSE response
                                        sessionService.recordAssistantTurn(
                                                request.sessionId(),
                                                responseAccumulator.toString(),
                                                request,
                                                System.currentTimeMillis() - startTime.get()
                                        ).subscribe()
                                )
                                .doOnError(e ->
                                        System.err.printf("[ContentTranslator] Stream error for session %s: %s%n",
                                                request.sessionId(), e.getMessage())
                                )
                );
    }
}
