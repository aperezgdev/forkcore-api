package com.forkcore.api.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdTest {

	@Test
	void shouldNotValidateWhenConstructingIdDirectly() {
		assertThat(new Id(null).value()).isNull();
	}

	@Test
	void shouldCreateIdsFromFactoryMethods() {
		var generatedId = Id.create();
		var uuid = UUID.randomUUID();

		assertThat(generatedId.value()).isNotNull();
		assertThat(Id.fromString(uuid.toString()).value()).isEqualTo(uuid);
	}
}
