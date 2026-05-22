package com.planet0088.universalCommunications.repository;

import com.planet0088.universalCommunications.document.SessionDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Reactive repository for SessionDocument.
 * findById / save / deleteById come for free from ReactiveMongoRepository.
 *
 * No custom queries needed for Sprint 1.
 * Sprint 2 will add findBySessionIdAndTenantId once auth is in.
 */
@Repository
public interface SessionRepository extends ReactiveMongoRepository<SessionDocument, String> {
}
