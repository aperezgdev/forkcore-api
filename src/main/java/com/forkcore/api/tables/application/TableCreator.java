package com.forkcore.api.tables.application;

import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.result.Result;
import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.domain.TableRepository;
import com.forkcore.api.tables.domain.vo.TableCode;
import org.springframework.stereotype.Service;

@Service
public class TableCreator {

	private final TableRepository tableRepository;

	public TableCreator(TableRepository tableRepository) {
		this.tableRepository = tableRepository;
	}

	public Result<Table> run(String code, Integer capacity, String location, String status) {
		var table = Table.create(code, capacity, location, status);
		if (table.isFailure()) {
			return table;
		}

		var codeVo = TableCode.from(code).value();
		var existing = tableRepository.findByCode(codeVo);
		if (existing.isPresent()) {
			return Result.failure(new ConflictError("code", "table code already exists"));
		}

		return Result.success(tableRepository.save(table.value()));
	}
}
