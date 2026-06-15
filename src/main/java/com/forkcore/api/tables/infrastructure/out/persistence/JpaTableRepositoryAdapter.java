package com.forkcore.api.tables.infrastructure.out.persistence;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.domain.TableRepository;
import com.forkcore.api.tables.domain.vo.TableCode;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTableRepositoryAdapter implements TableRepository {

	private final SpringDataTableJpaRepository repository;

	public JpaTableRepositoryAdapter(SpringDataTableJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Table save(Table table) {
		repository.save(TableJpaEntity.from(table));
		return table;
	}

	@Override
	public Optional<Table> findByCode(TableCode code) {
		return repository.findByCode(code.value()).map(TableJpaEntity::toDomain);
	}

	@Override
	public Optional<Table> findById(Id id) {
		return repository.findById(id.value()).map(TableJpaEntity::toDomain);
	}

	@Override
	public void delete(Table table) {
		repository.delete(TableJpaEntity.from(table));
	}
}
