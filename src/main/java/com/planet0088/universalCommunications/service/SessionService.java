package com.planet0088.universalCommunications.service;

import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.model.enums.OutputType;
import reactor.core.publisher.Mono;

public interface SessionService {

    Mono<Void> initSessionIfAbsent(String sessionId);

    Mono<Void> recordTranslation(String sessionId, InputType inputType, OutputType outputType,
                                  String rawInput, String translatedOutput);
}
