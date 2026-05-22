package com.planet0088.universalCommunications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Token budget configuration — all values tunable via application.yml.
 *
 * gpt-4o-mini context window = 128k tokens.
 * We stay well within safe limits to keep costs low.
 *
 * Breakdown:
 *   system prompt     ~100 tokens  (fixed)
 *   current message   ~200 tokens  (estimated max)
 *   response budget   1000 tokens  (max_tokens we request)
 *   history budget    2000 tokens  (what we allow for past messages)
 *   safety buffer      700 tokens
 *   ─────────────────────────────
 *   total used        ~4000 tokens worst case per request
 *   cost per request  ~$0.0006 at gpt-4o-mini pricing
 */
@Data
@Component
@ConfigurationProperties(prefix = "uacp.token")
public class TokenBudgetConfig {

    // Max tokens to spend on conversation history
    private int historyBudget = 2000;

    // Max tokens reserved for the model's response
    private int responseBudget = 1000;

    // Encoding name — must match the model being used
    // gpt-4o-mini uses "o200k_base" encoding
    private String encodingName = "o200k_base";
}