package com.forkcore.api.tables.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.domain.TableRepository;
import com.forkcore.api.tables.domain.vo.TableCode;
import com.forkcore.api.tables.infrastructure.out.persistence.InMemoryTableRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TableCreatorTest {

	@Test
	void shouldPersistCreatedTable() {
		var repository = new InMemoryTableRepository();
		var creator = new TableCreator(repository);

		var result = creator.run("T-01", 4, "Terraza", null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().code()).isEqualTo("T-01");
		assertThat(result.value().capacity()).isEqualTo(4);
		assertThat(result.value().location()).isEqualTo("Terraza");
		assertThat(result.value().status()).isEqualTo("available");
	}

	@Test
	void shouldReturnValidationFailureWithoutTouchingRepository() {
		var repository = new TableRepository() {
			@Override
			public Table save(Table table) {
				throw new AssertionError("save should not be called");
			}

			@Override
			public Optional<Table> findByCode(TableCode code) {
				throw new AssertionError("findByCode should not be called");
			}

			@Override
			public Optional<Table> findById(Id id) {
				throw new AssertionError("findById should not be called");
			}

			@Override
			public void delete(Table table) {
				throw new AssertionError("delete should not be called");
			}
		};
		var creator = new TableCreator(repository);

		var result = creator.run("", 0, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.containsExactly(
				new ValidationError("code", "table code is required"),
				new ValidationError("capacity", "table capacity must be greater than or equal to one")
			);
	}

	@Test
	void shouldReturnConflictWhenCodeAlreadyExists() {
		var repository = new InMemoryTableRepository();
		var initial = Table.create("T-01", 4, "Salon", "available").value();
		repository.save(initial);
		var creator = new TableCreator(repository);

		var result = creator.run("T-01", 8, "Patio", "available");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ConflictError.class);
		assertThat(result.error().message()).isEqualTo("table code already exists");
	}

	@Test
	void shouldSaveAndReturnWhenCodeIsUnique() {
		var repository = new InMemoryTableRepository();
		var creator = new TableCreator(repository);

		var result = creator.run("T-UNIQUE", 2, null, "available");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().code()).isEqualTo("T-UNIQUE");
		assertThat(result.value().capacity()).isEqualTo(2);
		assertThat(result.value().status()).isEqualTo("available");
		assertThat(repository.findByCode(TableCode.from("T-UNIQUE").value())).isPresent();
	}
}
