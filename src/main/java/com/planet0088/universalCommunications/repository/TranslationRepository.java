package com.planet0088.universalCommunications.repository;

import com.planet0088.universalCommunications.document.SessionMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TranslationRepository extends ReactiveMongoRepository<SessionMessage, String> {

    Flux<SessionMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    Flux<SessionMessage> findBySessionIdAndTenantIdOrderByTimestampAsc(String sessionId, String tenantId);
}
