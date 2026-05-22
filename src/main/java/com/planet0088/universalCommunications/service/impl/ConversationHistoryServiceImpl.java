package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.config.TokenBudgetConfig;
import com.planet0088.universalCommunications.document.SessionDocument;
import com.planet0088.universalCommunications.document.SessionMessage;
import com.planet0088.universalCommunications.repository.SessionRepository;
import com.planet0088.universalCommunications.service.ConversationHistoryService;
import com.planet0088.universalCommunications.util.TokenCounterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationHistoryServiceImpl implements ConversationHistoryService {

    private final SessionRepository sessionRepository;
    private final TokenCounterUtil tokenCounterUtil;
    private final TokenBudgetConfig tokenBudgetConfig;

    @Override
    public Mono<List<Message>> loadHistory(String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(session -> trimToBudget(session, tokenBudgetConfig.getHistoryBudget()))
                .defaultIfEmpty(Collections.emptyList())
                .doOnNext(history ->
                        log.debug("Loaded {} history messages for session {}",
                                history.size(), sessionId)
                );
    }

    /**
     * Trim strategy:
     *   1. Take all messages from the session (already time-ordered oldest→newest)
     *   2. Walk from NEWEST to OLDEST, accumulating token count
     *   3. Stop once budget is exceeded
     *   4. Reverse back to oldest→newest before returning
     *
     * This ensures we always keep the most RECENT context when trimming,
     * which is what matters most for conversation continuity.
     */
    private List<Message> trimToBudget(SessionDocument session, int budget) {
        List<SessionMessage> allMessages = session.getMessages();

        if (allMessages == null || allMessages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Message> selected = new ArrayList<>();
        int totalTokens = 0;
        int droppedCount = 0;

        // Walk newest → oldest
        for (int i = allMessages.size() - 1; i >= 0; i--) {
            SessionMessage msg = allMessages.get(i);
            String content = resolveContent(msg);

            if (content == null || content.isBlank()) continue;

            int msgTokens = tokenCounterUtil.countMessageTokens(msg.getRole(), content);

            if (totalTokens + msgTokens > budget) {
                // Budget exceeded — stop here, older messages are dropped
                droppedCount = i + 1;
                break;
            }

            totalTokens += msgTokens;
            selected.add(toSpringAiMessage(msg.getRole(), content));
        }

        if (droppedCount > 0) {
            log.info("Session {}: dropped {} older messages to stay within {}-token history budget. Used {} tokens.",
                    session.getSessionId(), droppedCount, budget, totalTokens);
        }

        // Reverse back to oldest → newest for correct LLM context order
        Collections.reverse(selected);
        return selected;
    }

    private String resolveContent(SessionMessage msg) {
        // user turn has rawPayload, assistant turn has fullResponse
        return "user".equals(msg.getRole())
                ? msg.getRawPayload()
                : msg.getFullResponse();
    }

    private Message toSpringAiMessage(String role, String content) {
        return "user".equals(role)
                ? new UserMessage(content)
                : new AssistantMessage(content);
    }
}