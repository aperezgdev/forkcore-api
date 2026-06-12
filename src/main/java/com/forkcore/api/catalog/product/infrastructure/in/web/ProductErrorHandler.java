package com.forkcore.api.catalog.product.infrastructure.in.web;

import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.DomainError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductErrorHandler {

	@ExceptionHandler(InvalidProductException.class)
	public ProblemDetail handleInvalidProductException(InvalidProductException exception) {
		var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problemDetail.setTitle("Invalid product");
		if (exception.error() instanceof CompositeValidationError compositeValidationError) {
			problemDetail.setProperty("errors", compositeValidationError.errors());
		}
		return problemDetail;
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ProblemDetail handleProductNotFoundException(ProductNotFoundException exception) {
		var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problemDetail.setTitle("Product not found");
		return problemDetail;
	}

	public static InvalidProductException invalidProduct(DomainError error) {
		return new InvalidProductException(error);
	}

	public static ProductNotFoundException productNotFound(DomainError error) {
		return new ProductNotFoundException(error);
	}

	public static final class InvalidProductException extends RuntimeException {

		private final DomainError error;

		private InvalidProductException(DomainError error) {
			super(error.message());
			this.error = error;
		}

		public DomainError error() {
			return error;
		}
	}

	public static final class ProductNotFoundException extends RuntimeException {

		private final DomainError error;

		private ProductNotFoundException(DomainError error) {
			super(error.message());
			this.error = error;
		}

		public DomainError error() {
			return error;
		}
	}
}
