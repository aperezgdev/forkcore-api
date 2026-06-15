package com.forkcore.api.tables.infrastructure.out.persistence;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.domain.TableRepository;
import com.forkcore.api.tables.domain.vo.TableCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryTableRepository implements TableRepository {

	private final List<Table> tables = new ArrayList<>();

	@Override
	public Table save(Table table) {
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
		return tables.stream()
			.filter(table -> table.id().equals(id))
			.findFirst();
	}

	@Override
	public void delete(Table table) {
		tables.removeIf(existing -> existing.id().equals(table.id()));
	}

	public void deleteAll() {
		tables.clear();
	}
}
