package com.forkcore.api.tables.infrastructure.out.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TableTestDataCleaner {

	private final JdbcTemplate jdbcTemplate;

	public TableTestDataCleaner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM tables");
	}
}
