package com.planet0088.universalCommunications.preprocessor;

import com.planet0088.universalCommunications.model.enums.InputType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface InputPreprocessor {

    InputType supports();

    Mono<String> preprocess(String payload);

    default Mono<String> preprocessFile(FilePart filePart) {
        return Mono.error(new UnsupportedOperationException(
                "File input not supported for InputType." + supports()
        ));
    }
}
