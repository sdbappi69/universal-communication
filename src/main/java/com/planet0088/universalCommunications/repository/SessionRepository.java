package com.planet0088.universalCommunications.repository;

import com.planet0088.universalCommunications.document.SessionDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends ReactiveMongoRepository<SessionDocument, String> {
}
