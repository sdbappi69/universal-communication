package com.planet0088.universalCommunications.controller;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.service.ContentTranslator;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
public class CommunicateController {

    private final ContentTranslator contentTranslator;

    public CommunicateController(ContentTranslator contentTranslator) {
        this.contentTranslator = contentTranslator;
    }

    @PostMapping(value = "/communicate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ContentChunk>> stream(@Valid @RequestBody CommunicateRequest request) {
        return contentTranslator
                .translate(request)
                .map(chunk -> ServerSentEvent
                        .<ContentChunk>builder()
                        .id(String.valueOf(chunk.seq()))
                        .event("chunk")
                        .data(chunk)
                        .build()
                )
                .concatWith(
                        // Terminal event so the client knows the stream is done
                        Flux.just(ServerSentEvent
                                .<ContentChunk>builder()
                                .event("done")
                                .data(new ContentChunk(request.sessionId(), null, "[DONE]", -1))
                                .build()
                        )
                )
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent
                                .<ContentChunk>builder()
                                .event("error")
                                .data(new ContentChunk(request.sessionId(), null, e.getMessage(), -1))
                                .build()
                ));
    }
}