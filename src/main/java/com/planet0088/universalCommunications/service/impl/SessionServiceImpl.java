package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.document.SessionDocument;
import com.planet0088.universalCommunications.document.SessionMessage;
import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.repository.SessionRepository;
import com.planet0088.universalCommunications.repository.TranslationRepository;
import com.planet0088.universalCommunications.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final TranslationRepository translationRepository;

    @Override
    public Mono<Void> initSessionIfAbsent(String sessionId) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.defer(() -> sessionRepository.save(SessionDocument.create(sessionId))))
                .then()
                .onErrorResume(e -> {
                    log.error("Failed to init session {}: {}", sessionId, e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> recordTranslation(String sessionId, InputType inputType, OutputType outputType,
                                         String rawInput, String translatedOutput) {
        log.info("Recording translation for session: {}", sessionId);
        return translationRepository.save(
                SessionMessage.builder()
                        .sessionId(sessionId)
                        .inputType(inputType)
                        .outputType(outputType)
                        .rawInput(rawInput)
                        .translatedOutput(translatedOutput)
                        .timestamp(Instant.now())
                        .build()
        )
        .doOnSuccess(saved -> log.info("Translation saved for session: {}", sessionId))
        .then()
        .onErrorResume(e -> {
            log.error("Failed to save translation for session {}: ", sessionId, e);
            return Mono.empty();
        });
    }
}
