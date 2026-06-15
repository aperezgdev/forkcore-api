package com.forkcore.api.tables.domain.vo;

public record TableLocation(String value) {

	public static TableLocation from(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return new TableLocation(value.trim());
	}
}
