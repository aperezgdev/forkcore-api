package com.forkcore.api.catalog.product.infrastructure.in.web;

import com.forkcore.api.catalog.product.domain.Product;
import java.math.BigDecimal;

public record ProductResponse(
	String id,
	String name,
	String description,
	BigDecimal price,
	String status
) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(
			product.id().asString(),
			product.name(),
			product.description(),
			product.price(),
			product.status()
		);
	}
}
