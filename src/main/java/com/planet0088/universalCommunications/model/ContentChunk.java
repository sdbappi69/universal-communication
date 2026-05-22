package com.planet0088.universalCommunications.model;

import com.planet0088.universalCommunications.model.enums.OutputType;

public record ContentChunk(

        String sessionId,

        // Which output type this chunk belongs to (TEXT, VOICE, SIGN, etc.)
        OutputType outputType,

        // The actual streamed content — token text for TEXT, CDN URL for VOICE/SIGN (later sprints)
        String content,

        // Monotonically increasing per session — lets the client reorder if needed
        int seq
) {}