package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.preprocessor.InputPreprocessorRouter;
import com.planet0088.universalCommunications.service.ContentTranslator;
import com.planet0088.universalCommunications.service.SessionService;
import com.planet0088.universalCommunications.transformer.OutputTransformerRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextContentTranslator implements ContentTranslator {

    private final SessionService sessionService;
    private final InputPreprocessorRouter preprocessorRouter;
    private final OutputTransformerRouter outputTransformerRouter;

    @Override
    public Flux<ContentChunk> translate(CommunicateRequest request) {
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

    public Flux<ContentChunk> translateFile(String sessionId, CommunicateRequest request, FilePart filePart) {
        return preprocessorRouter.routeFile(request.inputType(), filePart)
                .flatMapMany(transcript -> {
                    sessionService.initSessionIfAbsent(sessionId).subscribe();
                    return outputTransformerRouter.transform(transcript, sessionId, request.outputTypes())
                            .doOnNext(chunk -> {
                                if (chunk.outputType() != null) {
                                    sessionService.recordTranslation(
                                            sessionId,
                                            request.inputType(),
                                            chunk.outputType(),
                                            filePart.filename(),
                                            chunk.content()
                                    ).subscribe();
                                }
                            });
                });
    }
}
