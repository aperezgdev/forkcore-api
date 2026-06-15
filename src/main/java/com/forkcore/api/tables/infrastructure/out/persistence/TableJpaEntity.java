package com.forkcore.api.tables.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tables")
public class TableJpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, unique = true, length = 16)
	private String code;

	@Column(nullable = false)
	private Integer capacity;

	@Column(columnDefinition = "text")
	private String location;

	@Column(nullable = false)
	private String status;

	protected TableJpaEntity() {
	}

	private TableJpaEntity(UUID id, String code, Integer capacity, String location, String status) {
		this.id = id;
		this.code = code;
		this.capacity = capacity;
		this.location = location;
		this.status = status;
	}

	public static TableJpaEntity from(com.forkcore.api.tables.domain.Table table) {
		return new TableJpaEntity(
			table.id().value(),
			table.code(),
			table.capacity(),
			table.location(),
			table.status()
		);
	}

	public com.forkcore.api.tables.domain.Table toDomain() {
		return com.forkcore.api.tables.domain.Table.fromPrimitives(id.toString(), code, capacity, location, status);
	}
}
