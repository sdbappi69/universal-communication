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

@Slf4j
@Service
@RequiredArgsConstructor
public class TextContentConvertor implements ContentConvertor {

    private final SessionService sessionService;
    private final OutputTransformerRouter outputTransformerRouter;

    @Override
    public Flux<ContentChunk> convert(CommunicateRequest request) {
        String text = request.payload();

        sessionService.initSessionIfAbsent(request.sessionId()).subscribe();

        return outputTransformerRouter.transform(text, request.sessionId(), request.outputTypes())
                .doOnNext(chunk -> {
                    if (chunk.outputType() != null) {
                        sessionService.recordTranslation(
                                request.sessionId(),
                                request.inputType(),
                                chunk.outputType(),
                                text,
                                chunk.content()
                        ).subscribe();
                    }
                });
    }


}
