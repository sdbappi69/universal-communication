package com.planet0088.universalCommunications.preprocessor;

import com.planet0088.universalCommunications.model.CommunicateRequest;
import com.planet0088.universalCommunications.model.enums.InputType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InputPreprocessorRouter {

    private final Map<InputType, InputPreprocessor> preprocessors;

    public InputPreprocessorRouter(List<InputPreprocessor> preprocessors) {
        this.preprocessors = preprocessors.stream()
                .collect(Collectors.toMap(InputPreprocessor::supports, Function.identity()));
        log.info("Registered {} input preprocessors: {}", preprocessors.size(),
                this.preprocessors.keySet());
    }

    public Mono<String> route(CommunicateRequest request) {
        return resolve(request.inputType()).preprocess(request.payload());
    }

    public Mono<String> routeFile(InputType inputType, FilePart filePart) {
        return resolve(inputType).preprocessFile(filePart);
    }

    private InputPreprocessor resolve(InputType inputType) {
        InputPreprocessor pp = preprocessors.get(inputType);
        if (pp == null) {
            throw new UnsupportedOperationException(
                    "No preprocessor registered for InputType." + inputType);
        }
        return pp;
    }
}
