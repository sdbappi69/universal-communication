package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.service.ContentTranslator;
import com.planet0088.universalCommunications.service.ConversationHistoryService;
import com.planet0088.universalCommunications.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TextContentTranslator implements ContentTranslator {

    private final ChatClient chatClient;
    private final SessionService sessionService;
    private final ConversationHistoryService conversationHistoryService;

    public TextContentTranslator(ChatClient.Builder chatClientBuilder,
                                 SessionService sessionService,
                                 ConversationHistoryService conversationHistoryService) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are an accessible communication assistant.
                        Respond clearly and concisely. Adapt your language
                        to be as inclusive and easy to understand as possible.
                        """)
                .build();
        this.sessionService = sessionService;
        this.conversationHistoryService = conversationHistoryService;
    }

    @Override
    public Flux<ContentChunk> translate(CommunicateRequest request) {
        AtomicInteger seq = new AtomicInteger(0);
        StringBuilder responseAccumulator = new StringBuilder();
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

        return sessionService.recordUserTurn(request)
                .then(conversationHistoryService.loadHistory(request.sessionId()))
                .flatMapMany(history -> {

                    // Build full message list: history + current user message
                    List<Message> messages = new ArrayList<>(history);
                    messages.add(new UserMessage(request.payload()));

                    log.debug("Session {}: sending {} messages to LLM (including current)",
                            request.sessionId(), messages.size());

                    return chatClient
                            .prompt(new Prompt(messages))
                            .stream()
                            .content()
                            .filter(token -> !token.isBlank())
                            .map(token -> {
                                responseAccumulator.append(token);
                                return new ContentChunk(
                                        request.sessionId(),
                                        OutputType.TEXT,
                                        token,
                                        seq.getAndIncrement()
                                );
                            })
                            .doOnComplete(() ->
                                    sessionService.recordAssistantTurn(
                                            request.sessionId(),
                                            responseAccumulator.toString(),
                                            request,
                                            System.currentTimeMillis() - startTime.get()
                                    ).subscribe()
                            )
                            .doOnError(e ->
                                    log.error("Stream error for session {}: {}",
                                            request.sessionId(), e.getMessage())
                            );
                });
    }
}