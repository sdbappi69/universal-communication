package com.planet0088.universalCommunications.preprocessor.impl;

import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.preprocessor.InputPreprocessor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TextInputPreprocessor implements InputPreprocessor {

    @Override
    public InputType supports() {
        return InputType.TEXT;
    }

    @Override
    public Mono<String> preprocess(String payload) {
        return Mono.just(payload);
    }
}
