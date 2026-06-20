package com.forkcore.api.tables.application;

import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.result.Result;
import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.domain.TableRepository;
import com.forkcore.api.tables.domain.vo.TableCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TableCreator {

	private static final Logger LOG = LoggerFactory.getLogger(TableCreator.class);

	private final TableRepository tableRepository;

	public TableCreator(TableRepository tableRepository) {
		this.tableRepository = tableRepository;
	}

	public Result<Table> run(String code, Integer capacity, String location, String status) {
		var table = Table.create(code, capacity, location, status);
		if (table.isFailure()) {
			LOG.warn("Table creation failed: reason=domain_validation code={}", code);
			return table;
		}

		var codeVo = TableCode.from(code).value();
		var existing = tableRepository.findByCode(codeVo);
		if (existing.isPresent()) {
			LOG.warn("Table creation conflict: code already exists code={}", code);
			return Result.failure(new ConflictError("code", "table code already exists"));
		}

		var saved = tableRepository.save(table.value());
		LOG.info("Table created id={} code={}", saved.id().asString(), saved.code());
		return Result.success(saved);
	}
}
