package com.forkcore.api.orders.infrastructure.out.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderTestDataCleaner {

	private final JdbcTemplate jdbcTemplate;

	public OrderTestDataCleaner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM order_lines");
		jdbcTemplate.update("DELETE FROM orders");
	}
}
