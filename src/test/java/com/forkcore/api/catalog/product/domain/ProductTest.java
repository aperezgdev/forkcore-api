package com.forkcore.api.catalog.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.FieldUpdate;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductTest {

	@Test
	void shouldCreateProductWithActiveStatusByDefault() {
		var result = Product.create("Burger", "", new BigDecimal("12.50"), null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo("active");
		assertThat(result.value().description()).isEqualTo("");
	}

	@Test
	void shouldCreateProductWithProvidedStatus() {
		var result = Product.create("Burger", "", new BigDecimal("12.50"), "inactive");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo("inactive");
	}

	@Test
	void shouldRejectBlankName() {
		var result = Product.create("   ", "", new BigDecimal("12.50"), "active");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(List.of(new ValidationError("name", "product name is required")))
		);
	}

	@Test
	void shouldRejectNegativePrice() {
		var result = Product.create("Burger", "", new BigDecimal("-1.00"), "active");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error())
			.isEqualTo(
				new CompositeValidationError(
					List.of(
						new ValidationError(
							"price",
							"product price must be greater than or equal to zero"
						)
					)
				)
			);
	}

	@Test
	void shouldAccumulateValidationErrors() {
		var result = Product.create("   ", "", new BigDecimal("-1.00"), "active");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.containsExactly(
				new ValidationError("name", "product name is required"),
				new ValidationError(
					"price",
					"product price must be greater than or equal to zero"
				)
			);
	}

	@Test
	void shouldUpdateOnlyNameWhenOtherFieldsAreAbsent() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.of("Burger deluxe"),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().name()).isEqualTo("Burger deluxe");
		assertThat(result.value().description()).isEqualTo("Classic burger");
		assertThat(result.value().price()).isEqualByComparingTo("12.50");
		assertThat(result.value().status()).isEqualTo("active");
	}

	@Test
	void shouldPreserveProductIdWhenApplyingPartialUpdate() {
		var product = Product.fromPrimitives(
			"11111111-1111-1111-1111-111111111111",
			"Burger",
			"Classic burger",
			new BigDecimal("12.50"),
			"active"
		);

		var result = product.updateWith(
			FieldUpdate.of("Burger deluxe"),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().id().asString()).isEqualTo("11111111-1111-1111-1111-111111111111");
	}

	@Test
	void shouldSetDescriptionToNullWhenExplicitlyRequested() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.absent(),
			FieldUpdate.of((String) null),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().description()).isNull();
	}

	@Test
	void shouldSetDescriptionToEmptyStringWhenExplicitlyRequested() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.absent(),
			FieldUpdate.of(""),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().description()).isEqualTo("");
	}

	@Test
	void shouldKeepDescriptionWhenFieldIsAbsent() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().description()).isEqualTo("Classic burger");
	}

	@Test
	void shouldRejectPartialUpdateWithInvalidName() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.of("   "),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(List.of(new ValidationError("name", "product name is required")))
		);
	}

	@Test
	void shouldRejectPartialUpdateWithInvalidPrice() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.of(new BigDecimal("-1.00")),
			FieldUpdate.absent()
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(
				List.of(
					new ValidationError(
						"price",
						"product price must be greater than or equal to zero"
					)
				)
			)
		);
	}

	@Test
	void shouldRejectPartialUpdateWithInvalidStatus() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.of("archived")
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(List.of(new ValidationError("status", "product status is invalid")))
		);
	}

	@Test
	void shouldAccumulateErrorsForInvalidFieldsInPartialUpdate() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value();

		var result = product.updateWith(
			FieldUpdate.of("   "),
			FieldUpdate.absent(),
			FieldUpdate.of(new BigDecimal("-1.00")),
			FieldUpdate.absent()
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.containsExactly(
				new ValidationError("name", "product name is required"),
				new ValidationError(
					"price",
					"product price must be greater than or equal to zero"
				)
			);
	}
}
