package com.forkcore.api.tables.infrastructure.in.web;

import com.forkcore.api.tables.domain.Table;

public record TableResponse(
	String id,
	String code,
	Integer capacity,
	String location,
	String status
) {

	public static TableResponse from(Table table) {
		return new TableResponse(
			table.id().asString(),
			table.code(),
			table.capacity(),
			table.location(),
			table.status()
		);
	}
}
