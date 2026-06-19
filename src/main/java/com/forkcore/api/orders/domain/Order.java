package com.forkcore.api.orders.domain;

import com.forkcore.api.orders.domain.vo.OrderNotes;
import com.forkcore.api.orders.domain.vo.OrderStatus;
import com.forkcore.api.orders.domain.vo.OrderTotal;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Order {

	private final Id id;
	private final OrderStatus status;
	private final List<OrderLine> lines;
	private final Id tableId;
	private final OrderNotes notes;
	private final BigDecimal total;

	private Order(Id id, OrderStatus status, List<OrderLine> lines, Id tableId, OrderNotes notes, BigDecimal total) {
		this.id = id;
		this.status = status;
		this.lines = List.copyOf(lines);
		this.tableId = tableId;
		this.notes = notes;
		this.total = total;
	}

	public static Result<Order> create(List<OrderLineInput> lines, String tableId, String notes) {
		var errors = new ArrayList<ValidationError>();

		if (lines == null || lines.isEmpty()) {
			errors.add(new ValidationError("lines", "at least one line is required"));
			return Result.failure(CompositeValidationError.from(errors.toArray(ValidationError[]::new)));
		}

		var validatedLines = new ArrayList<OrderLine>();
		for (var input : lines) {
			var lineResult = OrderLine.create(input.productId(), input.quantity(), input.unitPrice());
			if (lineResult.isFailure()) {
				var composite = (CompositeValidationError) lineResult.error();
				errors.addAll(composite.errors());
			} else {
				validatedLines.add(lineResult.value());
			}
		}

		Id resolvedTableId = null;
		if (tableId != null) {
			var tableIdResult = Id.from(tableId);
			if (tableIdResult.isFailure()) {
				errors.add(new ValidationError("tableId", "must be a valid UUID"));
			} else {
				resolvedTableId = tableIdResult.value();
			}
		}

		var notesResult = OrderNotes.from(notes);

		BigDecimal computedTotal = null;
		if (!validatedLines.isEmpty()) {
			computedTotal = validatedLines.stream()
				.map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity().value())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			var totalResult = OrderTotal.from(computedTotal);
			if (totalResult.isFailure()) {
				errors.add(new ValidationError("total", ((ValidationError) totalResult.error()).message()));
			}
		}

		if (!errors.isEmpty()) {
			return Result.failure(CompositeValidationError.from(errors.toArray(ValidationError[]::new)));
		}

		return Result.success(
			new Order(
				Id.create(),
				OrderStatus.pending,
				validatedLines,
				resolvedTableId,
				notesResult.value(),
				computedTotal
			)
		);
	}

	public static Order fromPrimitives(String id, String status, List<OrderLine> lines, String tableId, String notes, BigDecimal total) {
		return new Order(
			new Id(UUID.fromString(id)),
			OrderStatus.from(status).value(),
			lines,
			tableId != null ? new Id(UUID.fromString(tableId)) : null,
			OrderNotes.from(notes).value(),
			total
		);
	}

	public Id id() {
		return id;
	}

	public OrderStatus status() {
		return status;
	}

	public List<OrderLine> lines() {
		return lines;
	}

	public Id tableId() {
		return tableId;
	}

	public String notes() {
		return notes == null ? null : notes.value();
	}

	public BigDecimal total() {
		return total;
	}
}
