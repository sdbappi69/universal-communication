package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.service.ContentConvertor;
import com.planet0088.universalCommunications.service.SessionService;
import com.planet0088.universalCommunications.transformer.OutputTransformerRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextContentConvertor implements ContentConvertor {

    private final SessionService sessionService;
    private final OutputTransformerRouter outputTransformerRouter;

    @Override
    public Flux<ContentChunk> convert(CommunicateRequest request) {
        String text = request.payload();

        // Chain session init into the reactive pipeline so Reactor Context
        // (containing tenantId from the JWT filter) flows through to SessionServiceImpl.
        return sessionService.initSessionIfAbsent(request.sessionId())
                .thenMany(
                        outputTransformerRouter.transform(text, request.sessionId(), request.outputTypes())
                                .flatMap(chunk -> {
                                    if (chunk.outputType() == null) {
                                        return Mono.just(chunk);
                                    }
                                    return sessionService.recordTranslation(
                                            request.sessionId(),
                                            request.inputType(),
                                            chunk.outputType(),
                                            text,
                                            chunk.content()
                                    ).thenReturn(chunk);
                                })
                );
    }
}
