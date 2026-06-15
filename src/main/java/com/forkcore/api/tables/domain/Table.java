package com.forkcore.api.tables.domain;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import com.forkcore.api.tables.domain.vo.TableCapacity;
import com.forkcore.api.tables.domain.vo.TableCode;
import com.forkcore.api.tables.domain.vo.TableLocation;
import com.forkcore.api.tables.domain.vo.TableStatus;
import java.util.stream.Stream;

public final class Table {

	private final Id id;
	private final TableCode code;
	private final TableCapacity capacity;
	private final TableLocation location;
	private final TableStatus status;

	private Table(Id id, TableCode code, TableCapacity capacity, TableLocation location, TableStatus status) {
		this.id = id;
		this.code = code;
		this.capacity = capacity;
		this.location = location;
		this.status = status;
	}

	public static Result<Table> create(String code, Integer capacity, String location, String status) {
		var codeResult = TableCode.from(code);
		var capacityResult = TableCapacity.from(capacity);
		var statusResult = TableStatus.from(status);

		var errors = Stream.of(codeResult, capacityResult, statusResult)
			.filter(Result::isFailure)
			.map(result -> (ValidationError) result.error())
			.toArray(ValidationError[]::new);

		if (errors.length > 0) {
			return Result.failure(CompositeValidationError.from(errors));
		}

		return Result.success(
			new Table(
				Id.create(),
				codeResult.value(),
				capacityResult.value(),
				TableLocation.from(location),
				statusResult.value()
			)
		);
	}

	public String code() {
		return code.value();
	}

	public Integer capacity() {
		return capacity.value();
	}

	public String location() {
		return location == null ? null : location.value();
	}

	public String status() {
		return status.value();
	}

	public Id id() {
		return id;
	}

	public TableCode codeVo() {
		return code;
	}

	public static Table fromPrimitives(String id, String code, Integer capacity, String location, String status) {
		return new Table(
			com.forkcore.api.shared.domain.Id.fromStringOrThrow(id),
			new TableCode(code),
			new TableCapacity(capacity),
			location == null ? null : new TableLocation(location),
			new TableStatus(status)
		);
	}
}
