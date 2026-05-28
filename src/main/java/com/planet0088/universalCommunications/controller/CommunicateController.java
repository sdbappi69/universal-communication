package com.planet0088.universalCommunications.controller;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.preprocessor.InputPreprocessorRouter;
import com.planet0088.universalCommunications.service.ContentTranslator;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class CommunicateController {

    private static final Set<InputType> FILE_TYPES = Set.of(InputType.VOICE, InputType.VIDEO);

    private final ContentTranslator contentTranslator;
    private final InputPreprocessorRouter preprocessorRouter;

    public CommunicateController(ContentTranslator contentTranslator,
                                 InputPreprocessorRouter preprocessorRouter) {
        this.contentTranslator = contentTranslator;
        this.preprocessorRouter = preprocessorRouter;
    }

    @PostMapping(
            value = "/communicate/stream",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<ContentChunk>> stream(
            @RequestPart("sessionId") String sessionId,
            @RequestPart("inputType") String inputTypeStr,
            @RequestPart("outputTypes") String outputTypesStr,
            @RequestPart(value = "payload", required = false) String payload,
            @RequestPart(value = "file", required = false) FilePart file
    ) {
        InputType inputType = InputType.valueOf(inputTypeStr.trim().toUpperCase());
        List<OutputType> outputTypes = Arrays.stream(outputTypesStr.split(","))
                .map(String::trim)
                .map(OutputType::valueOf)
                .toList();

        if (FILE_TYPES.contains(inputType)) {
            if (file == null) {
                return toSse(sessionId, Flux.error(
                        new IllegalArgumentException("file is required for VOICE/VIDEO input type")));
            }
            Flux<ContentChunk> chunks = preprocessorRouter.routeFile(inputType, file)
                    .flatMapMany(transcript -> {
                        CommunicateRequest request = new CommunicateRequest(
                                sessionId, inputType, outputTypes, transcript);
                        return contentTranslator.translate(request);
                    });
            return toSse(sessionId, chunks);
        } else {
            if (payload == null || payload.isBlank()) {
                return toSse(sessionId, Flux.error(
                        new IllegalArgumentException("payload is required for TEXT/SYMBOL/TOUCH input type")));
            }
            CommunicateRequest request = new CommunicateRequest(
                    sessionId, inputType, outputTypes, payload.trim());
            return toSse(sessionId, contentTranslator.translate(request));
        }
    }

    private Flux<ServerSentEvent<ContentChunk>> toSse(String sessionId, Flux<ContentChunk> chunks) {
        return chunks
                .map(chunk -> ServerSentEvent
                        .<ContentChunk>builder()
                        .id(String.valueOf(chunk.seq()))
                        .event("chunk")
                        .data(chunk)
                        .build()
                )
                .concatWith(Flux.just(ServerSentEvent
                        .<ContentChunk>builder()
                        .event("done")
                        .data(new ContentChunk(sessionId, null, "[DONE]", -1))
                        .build()
                ))
                .onErrorResume(e -> Flux.just(ServerSentEvent
                        .<ContentChunk>builder()
                        .event("error")
                        .data(new ContentChunk(sessionId, null, e.getMessage(), -1))
                        .build()
                ));
    }
}
