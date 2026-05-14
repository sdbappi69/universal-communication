package com.planet0088.universalCommunications.model;

import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.model.enums.OutputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CommunicateRequest(

        @NotBlank
        String sessionId,

        @NotNull
        InputType inputType,

        @NotNull
        List<OutputType> outputTypes,

        // For Sprint 1: text payload only. Later sprints will add base64 for voice/video.
        @NotBlank
        String payload
) {}