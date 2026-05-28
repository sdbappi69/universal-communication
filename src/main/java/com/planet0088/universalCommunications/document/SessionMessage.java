package com.planet0088.universalCommunications.document;

import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.model.enums.OutputType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "translations")
public class SessionMessage {

    @Id
    private String id;

    @Indexed
    @Field("session_id")
    private String sessionId;

    @Field("input_type")
    private InputType inputType;

    @Field("output_type")
    private OutputType outputType;

    @Field("raw_input")
    private String rawInput;

    @Field("translated_output")
    private String translatedOutput;

    @Field("timestamp")
    private Instant timestamp;
}
