package com.forkcore.api.orders.infrastructure.out.idempotency;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
public class InMemoryIdempotencyKeyStore implements IdempotencyKeyStore {

	private final Map<String, IdempotencyEntry> storage = new ConcurrentHashMap<>();
	private final Duration retention;
	private final Clock clock;

	public InMemoryIdempotencyKeyStore(Duration retention, Clock clock) {
		this.retention = retention;
		this.clock = clock;
	}

	@Override
	public Optional<IdempotencyEntry> find(String key) {
		var entry = storage.get(key);
		if (entry == null) {
			return Optional.empty();
		}

		if (clock.instant().isAfter(entry.createdAt().plus(retention))) {
			storage.remove(key);
			return Optional.empty();
		}

		return Optional.of(entry);
	}

	@Override
	public void store(String key, IdempotencyEntry entry) {
		storage.putIfAbsent(key, entry);
	}

	public void clear() {
		storage.clear();
	}
}
