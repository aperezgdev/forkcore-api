package com.forkcore.api.tables.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TableValueObjectsTest {

	// TableCode

	@Test
	void shouldRejectBlankTableCode() {
		assertThat(TableCode.from("").isFailure()).isTrue();
		assertThat(TableCode.from("   ").isFailure()).isTrue();
	}

	@Test
	void shouldRejectNullTableCode() {
		assertThat(TableCode.from(null).isFailure()).isTrue();
	}

	@Test
	void shouldRejectTableCodeLongerThan16() {
		assertThat(TableCode.from("AAAAAAAAAAAAAAAAA").isFailure()).isTrue();
	}

	@Test
	void shouldRejectTableCodeWithIllegalChars() {
		assertThat(TableCode.from("T.01").isFailure()).isTrue();
		assertThat(TableCode.from("T@ble").isFailure()).isTrue();
		assertThat(TableCode.from("T+01").isFailure()).isTrue();
	}

	@Test
	void shouldTrimAndAcceptValidTableCode() {
		var result = TableCode.from("  T-01  ");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo("T-01");
	}

	@Test
	void shouldAcceptTableCodeWithUnderscoreAndHyphen() {
		assertThat(TableCode.from("TABLE_01").isSuccess()).isTrue();
		assertThat(TableCode.from("TABLE-01").isSuccess()).isTrue();
	}

	// TableCapacity

	@Test
	void shouldRejectNullTableCapacity() {
		assertThat(TableCapacity.from(null).isFailure()).isTrue();
	}

	@Test
	void shouldRejectZeroTableCapacity() {
		var result = TableCapacity.from(0);
		assertThat(result.isFailure()).isTrue();
	}

	@Test
	void shouldRejectNegativeTableCapacity() {
		var result = TableCapacity.from(-1);
		assertThat(result.isFailure()).isTrue();
	}

	@Test
	void shouldAcceptPositiveTableCapacity() {
		var result = TableCapacity.from(4);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo(4);
	}

	@Test
	void shouldAcceptOneTableCapacity() {
		var result = TableCapacity.from(1);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo(1);
	}

	// TableLocation

	@Test
	void shouldReturnNullForNullTableLocation() {
		assertThat(TableLocation.from(null)).isNull();
	}

	@Test
	void shouldReturnNullForWhitespaceOnlyTableLocation() {
		assertThat(TableLocation.from("   ")).isNull();
	}

	@Test
	void shouldTrimAndAcceptNonBlankTableLocation() {
		var location = TableLocation.from("  Terraza  ");
		assertThat(location).isNotNull();
		assertThat(location.value()).isEqualTo("Terraza");
	}

	@Test
	void shouldAcceptEmptyStringAsNull() {
		assertThat(TableLocation.from("")).isNull();
	}

	// TableStatus

	@Test
	void shouldDefaultToAvailableWhenNull() {
		var result = TableStatus.from(null);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo("available");
	}

	@Test
	void shouldDefaultToAvailableWhenBlank() {
		var result = TableStatus.from("   ");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo("available");
	}

	@Test
	void shouldNormalizeToLowercase() {
		var result = TableStatus.from("OCCUPIED");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo("occupied");
	}

	@Test
	void shouldAcceptOutOfService() {
		var result = TableStatus.from("out_of_service");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo("out_of_service");
	}

	@Test
	void shouldRejectReserved() {
		var result = TableStatus.from("reserved");
		assertThat(result.isFailure()).isTrue();
	}

	@Test
	void shouldRejectInvalidStatus() {
		var result = TableStatus.from("invalid");
		assertThat(result.isFailure()).isTrue();
	}
}
