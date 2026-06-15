package com.forkcore.api.tables.application;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.result.Result;
import com.forkcore.api.tables.domain.TableRepository;
import org.springframework.stereotype.Service;

@Service
public class TableDeleter {

	private final TableRepository tableRepository;

	public TableDeleter(TableRepository tableRepository) {
		this.tableRepository = tableRepository;
	}

	public Result<Void> run(String id) {
		var idResult = Id.from(id);
		if (idResult.isFailure()) {
			return Result.failure(idResult.error());
		}

		var resolvedId = idResult.value();
		var existing = tableRepository.findById(resolvedId);
		if (existing.isEmpty()) {
			return Result.failure(new NotFoundError("Table", resolvedId.asString()));
		}

		tableRepository.delete(existing.get());
		return Result.success();
	}
}
