package com.planet0088.universalCommunications.transformer.impl;

import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.transformer.OutputTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextOutputTransformer implements OutputTransformer {

    @Override
    public OutputType supports() {
        return OutputType.TEXT;
    }

    @Override
    public Mono<ContentChunk> transform(String text, String sessionId, int seq) {
        return Mono.just(new ContentChunk(sessionId, OutputType.TEXT, text, seq));
    }
}
