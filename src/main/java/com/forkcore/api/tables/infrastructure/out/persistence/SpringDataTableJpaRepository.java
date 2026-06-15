package com.forkcore.api.tables.infrastructure.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTableJpaRepository extends JpaRepository<TableJpaEntity, UUID> {

	Optional<TableJpaEntity> findByCode(String code);
}
