package com.forkcore.api.orders.infrastructure.out.idempotency;

import java.util.Optional;

public interface IdempotencyKeyStore {

	Optional<IdempotencyEntry> find(String key);

	void store(String key, IdempotencyEntry entry);
}
