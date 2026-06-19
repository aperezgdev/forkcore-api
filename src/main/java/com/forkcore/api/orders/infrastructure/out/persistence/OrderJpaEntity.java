package com.forkcore.api.orders.infrastructure.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 32)
	private String status;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal total;

	@Column
	private UUID tableId;

	@Column(columnDefinition = "text")
	private String notes;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderLineJpaEntity> lines = new ArrayList<>();

	protected OrderJpaEntity() {
	}

	public OrderJpaEntity(UUID id, String status, BigDecimal total, UUID tableId, String notes, List<OrderLineJpaEntity> lines) {
		this.id = id;
		this.status = status;
		this.total = total;
		this.tableId = tableId;
		this.notes = notes;
		this.lines = lines;
	}

	public UUID id() {
		return id;
	}

	public String status() {
		return status;
	}

	public BigDecimal total() {
		return total;
	}

	public UUID tableId() {
		return tableId;
	}

	public String notes() {
		return notes;
	}

	public List<OrderLineJpaEntity> lines() {
		return lines;
	}
}
