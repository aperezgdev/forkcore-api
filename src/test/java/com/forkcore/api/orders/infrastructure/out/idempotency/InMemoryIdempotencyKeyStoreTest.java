package com.forkcore.api.orders.infrastructure.out.idempotency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryIdempotencyKeyStoreTest {

	private static final Duration RETENTION = Duration.ofHours(24);
	private Clock clock;
	private InMemoryIdempotencyKeyStore store;

	@BeforeEach
	void setUp() {
		clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
		store = new InMemoryIdempotencyKeyStore(RETENTION, clock);
	}

	@Test
	void shouldReturnEmptyForUnknownKey() {
		assertThat(store.find("unknown-key")).isEmpty();
	}

	@Test
	void shouldStoreAndRetrieveEntry() {
		var entry = new IdempotencyEntry("fingerprint", clock.instant(), 201, Map.of("Location", "/orders/123"), "body");

		store.store("key-1", entry);
		var result = store.find("key-1");

		assertThat(result).isPresent();
		assertThat(result.get().fingerprint()).isEqualTo("fingerprint");
		assertThat(result.get().status()).isEqualTo(201);
	}

	@Test
	void shouldNotOverwriteEntryOnSecondStore() {
		var entry1 = new IdempotencyEntry("fp1", clock.instant(), 201, Map.of("Location", "/orders/1"), "body1");
		var entry2 = new IdempotencyEntry("fp2", clock.instant(), 201, Map.of("Location", "/orders/2"), "body2");

		store.store("key-1", entry1);
		store.store("key-1", entry2);
		var result = store.find("key-1");

		assertThat(result).isPresent();
		assertThat(result.get().fingerprint()).isEqualTo("fp1");
	}

	@Test
	void shouldExpireEntryAfterRetentionPeriod() {
		var entry = new IdempotencyEntry("fingerprint", clock.instant(), 201, Map.of("Location", "/orders/123"), "body");
		store.store("key-1", entry);

		clock = Clock.fixed(clock.instant().plus(RETENTION).plus(Duration.ofMinutes(1)), ZoneId.of("UTC"));
		store = new InMemoryIdempotencyKeyStore(RETENTION, clock);

		store.store("key-1", entry);
		var result = store.find("key-1");

		assertThat(result).isEmpty();
	}

	@Test
	void shouldReturnEntryBeforeExpiry() {
		var entry = new IdempotencyEntry("fingerprint", clock.instant(), 201, Map.of("Location", "/orders/123"), "body");
		store.store("key-1", entry);

		clock = Clock.fixed(clock.instant().plus(Duration.ofHours(23)), ZoneId.of("UTC"));
		store = new InMemoryIdempotencyKeyStore(RETENTION, clock);

		store.store("key-1", entry);
		var result = store.find("key-1");

		assertThat(result).isPresent();
	}
}
