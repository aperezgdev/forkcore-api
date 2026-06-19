package com.forkcore.api.orders.infrastructure.out.idempotency;

import java.time.Instant;
import java.util.Map;

public record IdempotencyEntry(
	String fingerprint,
	Instant createdAt,
	int status,
	Map<String, String> headers,
	String body
) {
}
