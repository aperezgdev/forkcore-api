package com.forkcore.api.orders.infrastructure.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
