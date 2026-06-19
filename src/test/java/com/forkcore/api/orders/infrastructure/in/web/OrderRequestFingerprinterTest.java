package com.forkcore.api.orders.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderRequestFingerprinterTest {

	private OrderRequestFingerprinter fingerprinter;

	@BeforeEach
	void setUp() {
		fingerprinter = new OrderRequestFingerprinter();
	}

	@Test
	void shouldProduceSameFingerprintForSameContent() {
		var request1 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 2)),
			"table-1",
			"notes"
		);
		var request2 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 2)),
			"table-1",
			"notes"
		);

		assertThat(fingerprinter.fingerprint(request1))
			.isEqualTo(fingerprinter.fingerprint(request2));
	}

	@Test
	void shouldProduceDifferentFingerprintForDifferentQuantity() {
		var request1 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 2)),
			null,
			null
		);
		var request2 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 3)),
			null,
			null
		);

		assertThat(fingerprinter.fingerprint(request1))
			.isNotEqualTo(fingerprinter.fingerprint(request2));
	}

	@Test
	void shouldProduceDifferentFingerprintForDifferentProductId() {
		var request1 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 1)),
			null,
			null
		);
		var request2 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("22222222-2222-2222-2222-222222222222", 1)),
			null,
			null
		);

		assertThat(fingerprinter.fingerprint(request1))
			.isNotEqualTo(fingerprinter.fingerprint(request2));
	}

	@Test
	void shouldProduceDifferentFingerprintForDifferentTableId() {
		var request1 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 1)),
			"table-1",
			null
		);
		var request2 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 1)),
			"table-2",
			null
		);

		assertThat(fingerprinter.fingerprint(request1))
			.isNotEqualTo(fingerprinter.fingerprint(request2));
	}

	@Test
	void shouldExcludeNotesWhenNull() {
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 1)),
			null,
			null
		);

		var canonical = fingerprinter.buildCanonicalJson(request);
		assertThat(canonical).doesNotContain("notes");
	}

	@Test
	void shouldExcludeTableIdWhenNull() {
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 1)),
			null,
			null
		);

		var canonical = fingerprinter.buildCanonicalJson(request);
		assertThat(canonical).doesNotContain("tableId");
	}

	@Test
	void shouldIncludeTableIdWhenPresent() {
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 1)),
			"33333333-3333-3333-3333-333333333333",
			null
		);

		var canonical = fingerprinter.buildCanonicalJson(request);
		assertThat(canonical).contains("33333333-3333-3333-3333-333333333333");
	}

	@Test
	void shouldIncludeNotesWhenPresent() {
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("11111111-1111-1111-1111-111111111111", 1)),
			null,
			"sin cebolla"
		);

		var canonical = fingerprinter.buildCanonicalJson(request);
		assertThat(canonical).contains("sin cebolla");
	}
}
