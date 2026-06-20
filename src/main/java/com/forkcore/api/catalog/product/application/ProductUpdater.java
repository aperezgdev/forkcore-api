package com.forkcore.api.catalog.product.application;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.FieldUpdate;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductUpdater {

	private static final Logger LOG = LoggerFactory.getLogger(ProductUpdater.class);

	private final ProductRepository productRepository;

	public ProductUpdater(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Result<Product> run(
		String id,
		FieldUpdate<String> name,
		FieldUpdate<String> description,
		FieldUpdate<BigDecimal> price,
		FieldUpdate<String> status
	) {
		var idResult = Id.from(id);
		if (idResult.isFailure()) {
			return Result.failure(idResult.error());
		}

		var resolvedId = idResult.value();
		var existing = productRepository.findById(resolvedId);
		if (existing.isEmpty()) {
			LOG.debug("Product not found id={}", resolvedId.asString());
			return Result.failure(new NotFoundError("Product", resolvedId.asString()));
		}

		var updated = existing.get().updateWith(name, description, price, status);
		if (updated.isFailure()) {
			LOG.warn("Product update failed: id={} reason=domain_validation", resolvedId.asString());
			return updated;
		}

		var saved = productRepository.save(updated.value());
		LOG.info("Product updated id={}", saved.id().asString());
		return Result.success(saved);
	}
}
