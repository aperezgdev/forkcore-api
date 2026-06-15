package com.forkcore.api.tables.infrastructure.in.web;

import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.tables.application.TableCreator;
import com.forkcore.api.tables.application.TableDeleter;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tables")
public class TableController {

	private final TableCreator tableCreator;
	private final TableDeleter tableDeleter;

	public TableController(TableCreator tableCreator, TableDeleter tableDeleter) {
		this.tableCreator = tableCreator;
		this.tableDeleter = tableDeleter;
	}

	@PostMapping
	public ResponseEntity<?> create(@RequestBody CreateTableRequest request) {
		var result = tableCreator.run(request.code(), request.capacity(), request.location(), request.status());

		if (result.isSuccess()) {
			var response = TableResponse.from(result.value());
			return ResponseEntity.created(URI.create("/tables/" + response.id())).body(response);
		}

		if (result.error() instanceof CompositeValidationError cve) {
			var errors = cve.errors().stream()
				.map(e -> Map.of("field", e.field(), "message", e.message()))
				.toList();
			return ResponseEntity.badRequest().body(Map.of("errors", errors));
		}

		if (result.error() instanceof ConflictError ce) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("errors", List.of(Map.of("field", ce.field(), "message", ce.message()))));
		}

		return ResponseEntity.badRequest().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		var result = tableDeleter.run(id);
		if (result.isFailure()) {
			if (result.error() instanceof NotFoundError) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.noContent().build();
	}
}
