package com.planet0088.universalCommunications.transformer.impl;

import com.planet0088.universalCommunications.model.ContentChunk;
import com.planet0088.universalCommunications.model.enums.OutputType;
import com.planet0088.universalCommunications.transformer.OutputTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Base64;

@Slf4j
@Component
public class VoiceOutputTransformer implements OutputTransformer {

    private final OpenAiAudioSpeechModel speechModel;
    private final String voice;

    public VoiceOutputTransformer(OpenAiAudioSpeechModel speechModel,
                                   @Value("${uacp.tts.voice:alloy}") String voice) {
        this.speechModel = speechModel;
        this.voice = voice;
    }

    @Override
    public OutputType supports() {
        return OutputType.VOICE;
    }

    @Override
    public Mono<ContentChunk> transform(String text, String sessionId, int seq) {
        return Mono.fromCallable(() -> {
                    OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                            .model("tts-1")
                            .voice(voice)
                            .responseFormat("mp3")
                            .speed(1.0)
                            .build();
                    TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);
                    byte[] audio = speechModel.call(prompt).getResult().getOutput();
                    return Base64.getEncoder().encodeToString(audio);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(base64 -> new ContentChunk(sessionId, OutputType.VOICE, base64, seq))
                .onErrorResume(e -> {
                    log.error("TTS failed for session {}: {}", sessionId, e.getMessage());
                    return Mono.just(new ContentChunk(sessionId, OutputType.VOICE, "", seq));
                });
    }
}
