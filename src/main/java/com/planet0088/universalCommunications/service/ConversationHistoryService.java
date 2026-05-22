package com.planet0088.universalCommunications.service;

import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Responsible for loading conversation history from MongoDB
 * and trimming it to fit within the token budget.
 *
 * Returns a list of Spring AI Message objects ready to be
 * injected directly into the ChatClient prompt.
 */
public interface ConversationHistoryService {

    /**
     * Load previous messages for a session, trimmed to token budget.
     *
     * @param sessionId the session to load history for
     * @return Mono of Spring AI Message list, oldest-first order (correct for LLM context)
     */
    Mono<List<Message>> loadHistory(String sessionId);
}