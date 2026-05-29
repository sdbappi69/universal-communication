package com.planet0088.universalCommunications.service.impl;

import com.planet0088.universalCommunications.document.SessionDocument;
import com.planet0088.universalCommunications.document.SessionMessage;
import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.repository.SessionRepository;
import com.planet0088.universalCommunications.repository.TranslationRepository;
import com.planet0088.universalCommunications.security.TenantContext;
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
        log.info("initSessionIfAbsent called for sessionId: {}", sessionId);
        return TenantContext.getTenantId()
                .doOnNext(tenantId -> log.info("TenantId resolved: {}", tenantId))
                .switchIfEmpty(Mono.fromRunnable(() ->
                        log.warn("TenantId is EMPTY — Reactor Context not propagated for sessionId: {}", sessionId)))
                .flatMap(tenantId -> sessionRepository.findBySessionIdAndTenantId(sessionId, tenantId)
                        .switchIfEmpty(Mono.defer(() ->
                                sessionRepository.save(SessionDocument.create(sessionId, tenantId))
                                        .doOnSuccess(saved -> log.info("Saved successfully: {}", saved))))
                        .then()
                )
                .onErrorResume(e -> {
                    log.error("Full error: ", e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> recordTranslation(String sessionId, InputType inputType, OutputType outputType,
                                         String rawInput, String translatedOutput) {
        log.info("recordTranslation called for sessionId: {}", sessionId);
        return TenantContext.getTenantId()
                .doOnNext(tenantId -> log.info("TenantId resolved: {}", tenantId))
                .switchIfEmpty(Mono.fromRunnable(() ->
                        log.warn("TenantId is EMPTY — Reactor Context not propagated for sessionId: {}", sessionId)))
                .flatMap(tenantId -> translationRepository.save(
                        SessionMessage.builder()
                                .sessionId(sessionId)
                                .tenantId(tenantId)
                                .inputType(inputType)
                                .outputType(outputType)
                                .rawInput(rawInput)
                                .translatedOutput(translatedOutput)
                                .timestamp(Instant.now())
                                .build()
                ).doOnSuccess(saved -> log.info("Saved successfully: {}", saved)))
                .then()
                .onErrorResume(e -> {
                    log.error("Full error: ", e);
                    return Mono.empty();
                });
    }
}
