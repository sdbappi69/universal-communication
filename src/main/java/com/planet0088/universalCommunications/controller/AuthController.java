package com.planet0088.universalCommunications.controller;

import com.planet0088.universalCommunications.config.JwtConfig;
import com.planet0088.universalCommunications.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

// @DevOnly — this endpoint will be replaced by cloud identity provider in production.
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    public record TokenRequest(
            String userId,
            String tenantId,
            String email,
            String role
    ) {}

    public record TokenResponse(
            String token,
            String userId,
            String tenantId,
            long expiresIn
    ) {}

    @PostMapping("/token")
    public Mono<TokenResponse> generateToken(@RequestBody TokenRequest request) {
        log.info("Token requested for userId={} tenantId={}", request.userId(), request.tenantId());
        String token = jwtService.generateToken(
                request.userId(), request.tenantId(), request.email(), request.role());
        return Mono.just(new TokenResponse(
                token,
                request.userId(),
                request.tenantId(),
                jwtConfig.getExpirationMs()
        ));
    }
}
