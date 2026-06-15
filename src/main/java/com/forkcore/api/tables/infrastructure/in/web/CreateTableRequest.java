package com.forkcore.api.tables.infrastructure.in.web;

public record CreateTableRequest(
	String code,
	Integer capacity,
	String location,
	String status
) {
}
