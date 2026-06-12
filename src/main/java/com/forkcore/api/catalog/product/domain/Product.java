package com.forkcore.api.catalog.product.domain;

import com.forkcore.api.catalog.product.domain.vo.ProductDescription;
import com.forkcore.api.catalog.product.domain.vo.ProductName;
import com.forkcore.api.catalog.product.domain.vo.ProductPrice;
import com.forkcore.api.catalog.product.domain.vo.ProductStatus;
import com.forkcore.api.shared.domain.FieldUpdate;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.util.stream.Stream;

public final class Product {

	private final Id id;
	private final ProductName name;
	private final ProductDescription description;
	private final ProductPrice price;
	private final ProductStatus status;

	private Product(
		Id id,
		ProductName name,
		ProductDescription description,
		ProductPrice price,
		ProductStatus status
	) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.status = status;
	}

	public static Result<Product> create(String name, String description, java.math.BigDecimal price, String status) {
		var nameResult = ProductName.from(name);
		var priceResult = ProductPrice.from(price);
		var statusResult = ProductStatus.from(status);
		var errors = Stream.of(nameResult, priceResult, statusResult)
			.filter(Result::isFailure)
			.map(result -> (ValidationError) result.error())
			.toArray(ValidationError[]::new);

		if (errors.length > 0) {
			return Result.failure(CompositeValidationError.from(errors));
		}

		return Result.success(
			new Product(
				Id.create(),
				nameResult.value(),
				ProductDescription.from(description),
				priceResult.value(),
				statusResult.value()
			)
		);
	}

	public static Product fromPrimitives(
		String id,
		String name,
		String description,
		java.math.BigDecimal price,
		String status
	) {
		return new Product(
			Id.fromString(id),
			new ProductName(name),
			new ProductDescription(description),
			new ProductPrice(price),
			new ProductStatus(status)
		);
	}

	/**
	 * Applies a partial update to the product preserving the aggregate invariants.
	 * <p>
	 * Only the fields that are explicitly marked as present in the supplied
	 * {@link FieldUpdate} instances are considered for change. Omitted fields keep
	 * their current value. {@code name}, {@code price} and {@code status} are
	 * validated using the same value-object factories used by {@link #create(String, String, java.math.BigDecimal, String)}.
	 * {@code description} is allowed to be cleared (set to {@code null}) or to an
	 * empty string.
	 *
	 * @param name change request for the product name.
	 * @param description change request for the product description.
	 * @param price change request for the product price.
	 * @param status change request for the product status.
	 * @return a successful {@link Result} holding the updated product, or a failed
	 *         {@link Result} carrying a {@link CompositeValidationError} when at
	 *         least one provided value is invalid.
	 */
	public Result<Product> updateWith(
		FieldUpdate<String> name,
		FieldUpdate<String> description,
		FieldUpdate<java.math.BigDecimal> price,
		FieldUpdate<String> status
	) {
		var nameResult = name.present() ? ProductName.from(name.value()) : Result.success(this.name);
		var priceResult = price.present() ? ProductPrice.from(price.value()) : Result.success(this.price);
		var statusResult = status.present() ? ProductStatus.from(status.value()) : Result.success(this.status);

		var errors = Stream.of(nameResult, priceResult, statusResult)
			.filter(Result::isFailure)
			.map(result -> (ValidationError) result.error())
			.toArray(ValidationError[]::new);

		if (errors.length > 0) {
			return Result.failure(CompositeValidationError.from(errors));
		}

		var newDescription = description.present()
			? ProductDescription.from(description.value())
			: this.description;

		return Result.success(
			new Product(
				this.id,
				nameResult.value(),
				newDescription,
				priceResult.value(),
				statusResult.value()
			)
		);
	}

	public Id id() {
		return id;
	}

	public String name() {
		return name.value();
	}

	public String description() {
		return description == null ? null : description.value();
	}

	public java.math.BigDecimal price() {
		return price.value();
	}

	public String status() {
		return status.value();
	}
}
