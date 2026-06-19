package com.forkcore.api.orders.infrastructure.in.web;

import tools.jackson.databind.ObjectMapper;
import com.forkcore.api.orders.application.OrderCreator;
import com.forkcore.api.orders.application.input.CreateOrderLineInput;
import com.forkcore.api.orders.infrastructure.out.idempotency.IdempotencyEntry;
import com.forkcore.api.orders.infrastructure.out.idempotency.IdempotencyKeyStore;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

	private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 255;

	private final OrderCreator orderCreator;
	private final IdempotencyKeyStore idempotencyKeyStore;
	private final OrderRequestFingerprinter fingerprinter;
	private final ObjectMapper objectMapper;

	public OrderController(
		OrderCreator orderCreator,
		IdempotencyKeyStore idempotencyKeyStore,
		OrderRequestFingerprinter fingerprinter,
		ObjectMapper objectMapper
	) {
		this.orderCreator = orderCreator;
		this.idempotencyKeyStore = idempotencyKeyStore;
		this.fingerprinter = fingerprinter;
		this.objectMapper = objectMapper;
	}

	@PostMapping
	public ResponseEntity<?> create(
		@RequestBody CreateOrderRequest request,
		@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
	) {
		if (idempotencyKey != null && idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
			return ResponseEntity.badRequest().build();
		}

		boolean useIdempotency = idempotencyKey != null && !idempotencyKey.isBlank();

		if (useIdempotency) {
			var fingerprint = fingerprinter.fingerprint(request);
			var cached = idempotencyKeyStore.find(idempotencyKey);

			if (cached.isPresent()) {
				var entry = cached.get();
				if (entry.fingerprint().equals(fingerprint)) {
					var headers = entry.headers();
					var location = headers.get("Location");
					var response = ResponseEntity.status(entry.status());
					if (location != null) {
						response = response.header("Location", location);
					}
					return response.body(entry.body());
				} else {
					return ResponseEntity.status(HttpStatus.CONFLICT).build();
				}
			}

			var result = executeCreation(request);

			if (result.getStatusCode().is2xxSuccessful()) {
				var responseBody = result.getBody();
				var location = result.getHeaders().getLocation();
				var serializedBody = serializeBody(responseBody);
				var headers = Map.of(
					"Location", location != null ? location.toString() : ""
				);
				idempotencyKeyStore.store(idempotencyKey,
					new IdempotencyEntry(fingerprint, Instant.now(), result.getStatusCode().value(), headers, serializedBody));
			}

			return result;
		}

		return executeCreation(request);
	}

	private ResponseEntity<?> executeCreation(CreateOrderRequest request) {
		var inputLines = toInputLines(request);

		var result = orderCreator.run(inputLines, request.tableId(), request.notes());

		if (result.isFailure()) {
			return ResponseEntity.badRequest().build();
		}

		var order = result.value();
		var response = OrderResponse.from(order);
		var location = URI.create("/orders/" + order.id().asString());
		return ResponseEntity.created(location).body(response);
	}

	private String serializeBody(Object body) {
		if (body == null) {
			return "";
		}
		try {
			return objectMapper.writeValueAsString(body);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to serialize response body for idempotency cache", e);
		}
	}

	private static List<CreateOrderLineInput> toInputLines(CreateOrderRequest request) {
		if (request.lines() == null) {
			return List.of();
		}

		return request.lines().stream()
			.map(line -> new CreateOrderLineInput(line.productId(), line.quantity()))
			.toList();
	}
}
