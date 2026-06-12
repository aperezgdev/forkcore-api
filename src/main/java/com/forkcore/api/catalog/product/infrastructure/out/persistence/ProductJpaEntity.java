package com.forkcore.api.catalog.product.infrastructure.out.persistence;

import com.forkcore.api.catalog.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private String status;

	protected ProductJpaEntity() {
	}

	private ProductJpaEntity(UUID id, String name, String description, BigDecimal price, String status) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.status = status;
	}

	public static ProductJpaEntity from(Product product) {
		return new ProductJpaEntity(
			product.id().value(),
			product.name(),
			product.description(),
			product.price(),
			product.status()
		);
	}

	public String description() {
		return description;
	}

	public ProductJpaEntity withDescription(String newDescription) {
		return new ProductJpaEntity(this.id, this.name, newDescription, this.price, this.status);
	}

	public Product toDomain() {
		return Product.fromPrimitives(id.toString(), name, description, price, status);
	}
}
