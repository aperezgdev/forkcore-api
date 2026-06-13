package com.forkcore.api.catalog.product.application;

import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.result.Result;
import org.springframework.stereotype.Service;

@Service
public class ProductDeleter {

	private final ProductRepository productRepository;

	public ProductDeleter(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Result<Void> run(String id) {
		var idResult = Id.from(id);
		if (idResult.isFailure()) {
			return Result.failure(idResult.error());
		}

		var resolvedId = idResult.value();
		var existing = productRepository.findById(resolvedId);
		if (existing.isEmpty()) {
			return Result.failure(new NotFoundError("Product", resolvedId.asString()));
		}

		productRepository.delete(existing.get());
		return Result.success();
	}
}
