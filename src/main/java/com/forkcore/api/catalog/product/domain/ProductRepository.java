package com.forkcore.api.catalog.product.domain;

import com.forkcore.api.shared.domain.Id;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

	Product save(Product product);

	List<Product> findAll();

	List<Product> findByStatus(String status);

	Optional<Product> findById(Id id);

	void delete(Product product);
}
