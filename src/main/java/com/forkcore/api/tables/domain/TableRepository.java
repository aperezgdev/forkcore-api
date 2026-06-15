package com.forkcore.api.tables.domain;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.tables.domain.vo.TableCode;
import java.util.Optional;

public interface TableRepository {

	Table save(Table table);

	Optional<Table> findByCode(TableCode code);

	Optional<Table> findById(Id id);

	void delete(Table table);
}
