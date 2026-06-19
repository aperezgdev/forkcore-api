package com.forkcore.api.orders.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderNotesTest {

	@Test
	void shouldAcceptNormalNotes() {
		var result = OrderNotes.from("sin cebolla");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo("sin cebolla");
	}

	@Test
	void shouldTrimNotes() {
		var result = OrderNotes.from("  sin cebolla  ");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo("sin cebolla");
	}

	@Test
	void shouldReturnNullForNull() {
		var result = OrderNotes.from(null);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isNull();
	}

	@Test
	void shouldReturnNullForBlank() {
		var result = OrderNotes.from("   ");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isNull();
	}
}
