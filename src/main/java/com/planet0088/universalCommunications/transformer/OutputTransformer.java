package com.planet0088.universalCommunications.transformer;

import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import reactor.core.publisher.Mono;

public interface OutputTransformer {

    OutputType supports();

    Mono<ContentChunk> transform(String text, String sessionId, int seq);
}
