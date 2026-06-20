package com.forkcore.api.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.orders.domain.vo.OrderStatus;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class OrderTest {

	@Test
	void shouldCreateOrderWithOneLine() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 2, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo(OrderStatus.pending);
		assertThat(result.value().lines()).hasSize(1);
		assertThat(result.value().total()).isEqualByComparingTo("20.00");
		assertThat(result.value().tableId()).isNull();
		assertThat(result.value().notes()).isNull();
	}

	@Test
	void shouldCreateOrderWithMultipleLines() {
		var lines = List.of(
			new OrderLineInput("11111111-1111-1111-1111-111111111111", 2, new BigDecimal("10.00")),
			new OrderLineInput("22222222-2222-2222-2222-222222222222", 3, new BigDecimal("2.50"))
		);
		var result = Order.create(lines, null, null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().total()).isEqualByComparingTo("27.50");
		assertThat(result.value().lines()).hasSize(2);
	}

	@Test
	void shouldCreateOrderWithTableId() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, "33333333-3333-3333-3333-333333333333", null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().tableId().asString()).isEqualTo("33333333-3333-3333-3333-333333333333");
	}

	@Test
	void shouldCreateOrderWithNotes() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, "sin cebolla");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().notes()).isEqualTo("sin cebolla");
	}

	@Test
	void shouldCreateOrderWithNullTableId() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().tableId()).isNull();
	}

	@Test
	void shouldCreateOrderWithBlankNotesTreatedAsNull() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, "   ");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().notes()).isNull();
	}

	@Test
	void shouldRejectNullLines() {
		var result = Order.create(null, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.containsExactly(new ValidationError("lines", "at least one line is required"));
	}

	@Test
	void shouldRejectEmptyLines() {
		var result = Order.create(List.of(), null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.containsExactly(new ValidationError("lines", "at least one line is required"));
	}

	@Test
	void shouldSetStatusToPending() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo(OrderStatus.pending);
	}

	@Test
	void shouldCalculateTotalCorrectly() {
		var lines = List.of(
			new OrderLineInput("11111111-1111-1111-1111-111111111111", 3, new BigDecimal("10.00")),
			new OrderLineInput("22222222-2222-2222-2222-222222222222", 2, new BigDecimal("2.20"))
		);
		var result = Order.create(lines, null, null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().total()).isEqualByComparingTo("34.40");
	}

	@Test
	void shouldRejectLineWithInvalidProductId() {
		var lines = List.of(new OrderLineInput("not-a-uuid", 1, new BigDecimal("10.00")));
		var result = Order.create(lines, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("id"));
	}

	@Test
	void shouldRejectLineWithInvalidQuantity() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 0, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("quantity"));
	}

	@Test
	void shouldRejectLineWithNegativeQuantity() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", -1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("quantity"));
	}

	@Test
	void shouldRejectLineWithNullQuantity() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", null, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, null);

		assertThat(result.isFailure()).isTrue();
	}

	@Test
	void shouldRejectLineWithNegativeUnitPrice() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("-1.00")
		));
		var result = Order.create(lines, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("price"));
	}

	@Test
	void shouldRejectInvalidTableId() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, "not-a-uuid", null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("tableId"));
	}

	@Test
	void shouldAccumulateErrorsAcrossMultipleLines() {
		var lines = List.of(
			new OrderLineInput("not-a-uuid", 0, new BigDecimal("-1.00")),
			new OrderLineInput("bad-uuid", -1, null)
		);
		var result = Order.create(lines, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors()).hasSize(6);
	}

	@Test
	void shouldAccumulateErrorsAcrossLinesAndTableId() {
		var lines = List.of(
			new OrderLineInput("11111111-1111-1111-1111-111111111111", 0, new BigDecimal("10.00"))
		);
		var result = Order.create(lines, "bad-table-id", null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors()).hasSize(2);
	}

	@Test
	void shouldReturnImmutableLines() {
		var lines = List.of(new OrderLineInput(
			"11111111-1111-1111-1111-111111111111", 1, new BigDecimal("10.00")
		));
		var result = Order.create(lines, null, null);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().lines()).isNotNull();
	}

	// --- changeStatus tests ---

	private static Order anOrderWithStatus(OrderStatus status) {
		var lines = List.of(OrderLine.fromPrimitives(
			"11111111-1111-1111-1111-111111111111",
			"22222222-2222-2222-2222-222222222222",
			2,
			new BigDecimal("10.00")
		));
		return Order.fromPrimitives(
			"33333333-3333-3333-3333-333333333333",
			status.name(),
			lines,
			null,
			null,
			new BigDecimal("20.00")
		);
	}

	@ParameterizedTest
	@CsvSource({
		"pending, in_progress",
		"pending, cancelled",
		"in_progress, ready",
		"in_progress, cancelled",
		"ready, delivered",
		"ready, cancelled"
	})
	void shouldTransitionToValidTargetStatus(OrderStatus current, OrderStatus target) {
		var order = anOrderWithStatus(current);
		var result = order.changeStatus(target);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo(target);
		assertThat(result.value()).isSameAs(order);
	}

	@ParameterizedTest
	@EnumSource(OrderStatus.class)
	void shouldReturnSuccessWhenTransitioningToSameState(OrderStatus status) {
		var order = anOrderWithStatus(status);
		var result = order.changeStatus(status);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo(status);
		assertThat(result.value()).isSameAs(order);
	}

	@ParameterizedTest
	@CsvSource({
		"pending, ready",
		"pending, delivered",
		"in_progress, pending",
		"in_progress, delivered",
		"ready, in_progress",
		"ready, pending",
		"delivered, in_progress",
		"delivered, ready",
		"delivered, cancelled",
		"cancelled, in_progress",
		"cancelled, ready",
		"cancelled, delivered",
		"cancelled, pending"
	})
	void shouldRejectInvalidTransition(OrderStatus current, OrderStatus target) {
		var order = anOrderWithStatus(current);
		var result = order.changeStatus(target);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ConflictError.class);
		assertThat(((ConflictError) result.error()).field()).isEqualTo("order.status.transition");
	}
}
