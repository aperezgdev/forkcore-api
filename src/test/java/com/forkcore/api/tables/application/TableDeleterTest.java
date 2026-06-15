package com.forkcore.api.tables.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.domain.TableRepository;
import com.forkcore.api.tables.domain.vo.TableCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TableDeleterTest {

	@Test
	void shouldDeleteExistingTableAndReturnSuccess() {
		var repository = new InMemoryRepository(
			Table.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"T-01",
				4,
				"Terraza",
				"available"
			)
		);
		var deleter = new TableDeleter(repository);

		var result = deleter.run("11111111-1111-1111-1111-111111111111");

		assertThat(result.isSuccess()).isTrue();
		assertThat(repository.findById(Id.fromStringOrThrow("11111111-1111-1111-1111-111111111111"))).isEmpty();
	}

	@Test
	void shouldReturnNotFoundErrorWhenTableDoesNotExist() {
		var repository = new InMemoryRepository();
		var deleter = new TableDeleter(repository);

		var result = deleter.run("99999999-9999-9999-9999-999999999999");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(NotFoundError.class);
		var notFoundError = (NotFoundError) result.error();
		assertThat(notFoundError.resource()).isEqualTo("Table");
		assertThat(notFoundError.id()).isEqualTo("99999999-9999-9999-9999-999999999999");
	}

	@Test
	void shouldPropagateValidationErrorWhenIdIsMalformed() {
		var repository = new InMemoryRepository();
		var deleter = new TableDeleter(repository);

		var result = deleter.run("not-a-uuid");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ValidationError.class);
		assertThat(((ValidationError) result.error()).field()).isEqualTo("id");
	}

	private static final class InMemoryRepository implements TableRepository {

		private final List<Table> tables = new ArrayList<>();

		private InMemoryRepository(Table... initial) {
			tables.addAll(List.of(initial));
		}

		@Override
		public Table save(Table table) {
			tables.removeIf(existing -> existing.id().equals(table.id()));
			tables.add(table);
			return table;
		}

		@Override
		public Optional<Table> findByCode(TableCode code) {
			return tables.stream()
				.filter(table -> table.codeVo().value().equals(code.value()))
				.findFirst();
		}

		@Override
		public Optional<Table> findById(Id id) {
			return tables.stream().filter(table -> table.id().equals(id)).findFirst();
		}

		@Override
		public void delete(Table table) {
			tables.removeIf(existing -> existing.id().equals(table.id()));
		}
	}
}
