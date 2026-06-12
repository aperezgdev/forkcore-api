package com.forkcore.api.catalog.product.infrastructure.in.web;

import java.math.BigDecimal;

public record CreateProductRequest(
	String name,
	String description,
	BigDecimal price,
	String status
) {
}
