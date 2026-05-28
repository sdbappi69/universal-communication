package com.planet0088.universalCommunications.preprocessor.impl;

import com.planet0088.universalCommunications.model.enums.InputType;
import com.planet0088.universalCommunications.preprocessor.InputPreprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceInputPreprocessor implements InputPreprocessor {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    @Override
    public InputType supports() {
        return InputType.VOICE;
    }

    @Override
    public Mono<String> preprocess(String payload) {
        return Mono.error(new UnsupportedOperationException(
                "VoiceInputPreprocessor requires an audio file — use the /stream/file endpoint"));
    }

    @Override
    public Mono<String> preprocessFile(FilePart filePart) {
        return Mono.fromCallable(() -> createTempFile(filePart.filename()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tempPath -> filePart.transferTo(tempPath).thenReturn(tempPath))
                .flatMap(this::transcribeAndCleanup);
    }

    private Path createTempFile(String originalFilename) throws IOException {
        String ext = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".wav";
        return Files.createTempFile("uacp-voice-", ext);
    }

    private Mono<String> transcribeAndCleanup(Path tempPath) {
        return Mono.fromCallable(() -> {
            try {
                FileSystemResource resource = new FileSystemResource(tempPath.toFile());
                String transcript = transcriptionModel
                        .call(new AudioTranscriptionPrompt(resource))
                        .getResult()
                        .getOutput();
                log.debug("Whisper transcribed {} chars", transcript.length());
                return transcript;
            } finally {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException e) {
                    log.warn("Could not delete temp file {}: {}", tempPath, e.getMessage());
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
