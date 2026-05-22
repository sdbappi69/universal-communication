package com.planet0088.universalCommunications.util;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.planet0088.universalCommunications.config.TokenBudgetConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Accurate token counter using jtokkit — same tokenizer OpenAI uses.
 *
 * Why not just estimate by word count?
 * OpenAI charges by token, not word. A word like "unfortunately" is 3 tokens.
 * Underestimating burns budget; overestimating wastes context. jtokkit is exact.
 *
 * Thread-safe: Encoding is stateless after init.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCounterUtil {

    private final TokenBudgetConfig tokenBudgetConfig;
    private Encoding encoding;

    @PostConstruct
    public void init() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        // getEncoding returns Optional — fall back to cl100k_base if not found
        this.encoding = registry.getEncoding(tokenBudgetConfig.getEncodingName())
                .orElseGet(() -> {
                    log.warn("Encoding '{}' not found, falling back to cl100k_base",
                            tokenBudgetConfig.getEncodingName());
                    return registry.getEncoding("cl100k_base").orElseThrow();
                });
        log.info("TokenCounterUtil initialized with encoding: {}", tokenBudgetConfig.getEncodingName());
    }

    /**
     * Count tokens in a single string.
     */
    public int countTokens(String text) {
        if (text == null || text.isBlank()) return 0;
        return encoding.countTokensOrdinary(text);
    }

    /**
     * Count tokens for a chat message including OpenAI's per-message overhead.
     * OpenAI adds ~4 tokens per message for role + formatting metadata.
     */
    public int countMessageTokens(String role, String content) {
        return countTokens(content) + countTokens(role) + 4;
    }
}