package com.forkcore.api.tables.infrastructure.out.persistence;

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

	public void deleteAll() {
		tables.clear();
	}
}
