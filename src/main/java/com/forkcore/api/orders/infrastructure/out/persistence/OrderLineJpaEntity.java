package com.forkcore.api.orders.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_lines")
public class OrderLineJpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderJpaEntity order;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	protected OrderLineJpaEntity() {
	}

	public OrderLineJpaEntity(UUID id, OrderJpaEntity order, UUID productId, int quantity, BigDecimal unitPrice) {
		this.id = id;
		this.order = order;
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public UUID id() {
		return id;
	}

	public OrderJpaEntity order() {
		return order;
	}

	public UUID productId() {
		return productId;
	}

	public int quantity() {
		return quantity;
	}

	public BigDecimal unitPrice() {
		return unitPrice;
	}
}
