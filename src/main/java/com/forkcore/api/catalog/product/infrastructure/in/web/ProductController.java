package com.forkcore.api.catalog.product.infrastructure.in.web;

import com.forkcore.api.catalog.product.application.ProductCreator;
import com.forkcore.api.catalog.product.application.ProductRetriever;
import com.forkcore.api.catalog.product.application.ProductUpdater;
import com.forkcore.api.shared.domain.FieldUpdate;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.NotFoundError;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductCreator productCreator;
	private final ProductRetriever productRetriever;
	private final ProductUpdater productUpdater;

	public ProductController(
		ProductCreator productCreator,
		ProductRetriever productRetriever,
		ProductUpdater productUpdater
	) {
		this.productCreator = productCreator;
		this.productRetriever = productRetriever;
		this.productUpdater = productUpdater;
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> get(@RequestParam(required = false) String status) {
		var result = productRetriever.run(status);
		if (result.isFailure()) {
			throw ProductErrorHandler.invalidProduct(result.error());
		}

		var response = result.value().stream().map(ProductResponse::from).toList();
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request) {
		var result = productCreator.run(
			request.name(),
			request.description(),
			request.price(),
			request.status()
		);
		if (result.isFailure()) {
			throw ProductErrorHandler.invalidProduct(result.error());
		}

		var response = ProductResponse.from(result.value());
		return ResponseEntity.created(URI.create("/products/" + response.id())).body(response);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ProductResponse> update(
		@PathVariable String id,
		@RequestBody UpdateProductRequest request
	) {
		var result = productUpdater.run(
			Id.fromString(id),
			toFieldUpdate(request.name()),
			toFieldUpdate(request.description()),
			toFieldUpdate(request.price()),
			toFieldUpdate(request.status())
		);
		if (result.isFailure()) {
			if (result.error() instanceof NotFoundError) {
				throw ProductErrorHandler.productNotFound(result.error());
			}
			throw ProductErrorHandler.invalidProduct(result.error());
		}

		return ResponseEntity.ok(ProductResponse.from(result.value()));
	}

	private static <T> FieldUpdate<T> toFieldUpdate(org.openapitools.jackson.nullable.JsonNullable<T> jsonNullable) {
		if (jsonNullable == null || !jsonNullable.isPresent()) {
			return FieldUpdate.absent();
		}
		return FieldUpdate.of(jsonNullable.get());
	}
}
