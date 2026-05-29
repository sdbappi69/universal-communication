package com.planet0088.universalCommunications.security;

import reactor.core.publisher.Mono;

public class TenantContext {

    public static final String TENANT_ID_KEY = "tenantId";
    public static final String USER_ID_KEY = "userId";

    private TenantContext() {}

    public static Mono<String> getTenantId() {
        return Mono.deferContextual(ctx ->
                Mono.justOrEmpty(ctx.getOrEmpty(TENANT_ID_KEY))
        );
    }

    public static Mono<String> getUserId() {
        return Mono.deferContextual(ctx ->
                Mono.justOrEmpty(ctx.getOrEmpty(USER_ID_KEY))
        );
    }
}
