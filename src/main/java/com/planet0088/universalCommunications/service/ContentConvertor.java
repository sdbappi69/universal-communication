package com.planet0088.universalCommunications.service;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import reactor.core.publisher.Flux;

/**
 * Core translation contract for the Universal Communications platform.
 *
 * Takes a {@link CommunicateRequest} containing an input type, one or more desired
 * output types, and a payload — and returns a reactive stream of {@link ContentChunk}
 * objects, one per token (for text) or one per media artifact (for voice/sign in later sprints).
 *
 * Implementations are responsible for:
 *   - Preprocessing the input (STT, vision, symbol decoding, etc.)
 *   - Routing to the correct AI model via Spring AI
 *   - Transforming model output into the requested output types
 *   - Writing each chunk to the session store asynchronously (fire-and-forget)
 *
 * The returned Flux must never block. All I/O must be non-blocking reactive.
 */
public interface ContentConvertor {

    /**
     * Translates the incoming request into a streamed sequence of content chunks.
     *
     * @param request the communication request containing inputType, outputTypes, payload, sessionId
     * @return a cold Flux that begins streaming once subscribed (i.e. when the SSE connection opens)
     */
    Flux<ContentChunk> convert(CommunicateRequest request);
}