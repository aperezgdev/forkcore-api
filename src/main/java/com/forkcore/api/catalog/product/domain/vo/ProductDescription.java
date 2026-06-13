package com.forkcore.api.catalog.product.domain.vo;

public record ProductDescription(String value) {

	public static ProductDescription from(String value) {
		return value == null ? null : new ProductDescription(value.trim());
	}
}
