package com.forkcore.api.catalog.product.infrastructure.out.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductTestDataCleaner {

	private final JdbcTemplate jdbcTemplate;

	public ProductTestDataCleaner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM products");
	}
}
