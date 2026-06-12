package com.forkcore.api.shared.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

public record Id(UUID value) {

	public static Id create() {
		return new Id(UuidCreator.getTimeOrderedEpoch());
	}

	public static Id fromString(String value) {
		return new Id(UUID.fromString(value));
	}

	public String asString() {
		return value.toString();
	}
}
