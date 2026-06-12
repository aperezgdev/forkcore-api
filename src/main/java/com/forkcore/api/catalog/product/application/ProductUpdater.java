package com.forkcore.api.catalog.product.application;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.FieldUpdate;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ProductUpdater {

	private final ProductRepository productRepository;

	public ProductUpdater(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Result<Product> run(
		Id id,
		FieldUpdate<String> name,
		FieldUpdate<String> description,
		FieldUpdate<BigDecimal> price,
		FieldUpdate<String> status
	) {
		var existing = productRepository.findById(id);
		if (existing.isEmpty()) {
			return Result.failure(new NotFoundError("Product", id.asString()));
		}

		var updated = existing.get().updateWith(name, description, price, status);
		if (updated.isFailure()) {
			return updated;
		}

		return Result.success(productRepository.save(updated.value()));
	}
}
