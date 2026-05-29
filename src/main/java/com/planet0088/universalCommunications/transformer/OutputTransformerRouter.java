package com.planet0088.universalCommunications.transformer;

import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OutputTransformerRouter {

    private final Map<OutputType, OutputTransformer> transformers;

    public OutputTransformerRouter(List<OutputTransformer> transformers) {
        this.transformers = transformers.stream()
                .collect(Collectors.toMap(OutputTransformer::supports, Function.identity()));
    }

    public Flux<ContentChunk> transform(String text, String sessionId, List<OutputType> outputTypes) {
        AtomicInteger seq = new AtomicInteger(0);
        return Flux.merge(
                outputTypes.stream()
                        .map(type -> resolve(type).transform(text, sessionId, seq.getAndIncrement()))
                        .toList()
        );
    }

    private OutputTransformer resolve(OutputType outputType) {
        OutputTransformer transformer = transformers.get(outputType);
        if (transformer == null) {
            throw new IllegalArgumentException("No transformer registered for OutputType." + outputType);
        }
        return transformer;
    }
}
