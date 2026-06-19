package com.forkcore.api.orders.infrastructure.in.web;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orders.idempotency")
public record IdempotencyProperties(Duration retention) {

	public IdempotencyProperties {
		if (retention == null) {
			retention = Duration.ofHours(24);
		}
	}
}
