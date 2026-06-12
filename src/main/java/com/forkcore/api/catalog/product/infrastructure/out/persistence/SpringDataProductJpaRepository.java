package com.forkcore.api.catalog.product.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

	List<ProductJpaEntity> findAllByOrderByNameAsc();

	List<ProductJpaEntity> findByStatusOrderByNameAsc(String status);
}
