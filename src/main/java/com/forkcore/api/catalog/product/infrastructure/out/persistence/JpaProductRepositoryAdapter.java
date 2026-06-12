package com.forkcore.api.catalog.product.infrastructure.out.persistence;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {

	private final SpringDataProductJpaRepository repository;

	public JpaProductRepositoryAdapter(SpringDataProductJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Product save(Product product) {
		var entity = ProductJpaEntity.from(product);
		var stored = entity.description() == null ? entity.withDescription("") : entity;
		repository.save(stored);
		return product;
	}

	@Override
	public List<Product> findAll() {
		return repository.findAllByOrderByNameAsc().stream().map(ProductJpaEntity::toDomain).toList();
	}

	@Override
	public List<Product> findByStatus(String status) {
		return repository.findByStatusOrderByNameAsc(status).stream().map(ProductJpaEntity::toDomain).toList();
	}

	@Override
	public Optional<Product> findById(Id id) {
		return repository.findById(id.value()).map(ProductJpaEntity::toDomain);
	}
}
