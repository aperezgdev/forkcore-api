package com.forkcore.api.catalog.product.infrastructure.out.persistence;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepository implements ProductRepository {

	private final Map<String, Product> storage = new ConcurrentHashMap<>();

	@Override
	public Product save(Product product) {
		storage.put(product.id().asString(), product);
		return product;
	}

	@Override
	public List<Product> findAll() {
		return storage.values().stream().sorted(Comparator.comparing(Product::name)).toList();
	}

	@Override
	public List<Product> findByStatus(String status) {
		return storage.values().stream()
			.filter(product -> product.status().equals(status))
			.sorted(Comparator.comparing(Product::name))
			.toList();
	}

	@Override
	public Optional<Product> findById(Id id) {
		return Optional.ofNullable(storage.get(id.asString()));
	}

	@Override
	public void delete(Product product) {
		storage.remove(product.id().asString());
	}

	public void deleteAll() {
		storage.clear();
	}
}
