package com.planet0088.universalCommunications.repository;

import com.planet0088.universalCommunications.document.SessionDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SessionRepository extends ReactiveMongoRepository<SessionDocument, String> {

    Mono<SessionDocument> findBySessionIdAndTenantId(String sessionId, String tenantId);
}
