package com.forkcore.api.orders.infrastructure;

import com.forkcore.api.orders.infrastructure.in.web.IdempotencyProperties;
import com.forkcore.api.orders.infrastructure.out.idempotency.IdempotencyKeyStore;
import com.forkcore.api.orders.infrastructure.out.idempotency.InMemoryIdempotencyKeyStore;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IdempotencyProperties.class)
public class OrdersConfiguration {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public IdempotencyKeyStore idempotencyKeyStore(IdempotencyProperties properties, Clock clock) {
		return new InMemoryIdempotencyKeyStore(properties.retention(), clock);
	}
}
