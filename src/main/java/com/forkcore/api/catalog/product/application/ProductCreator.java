package com.forkcore.api.catalog.product.application;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductCreator {

	private static final Logger LOG = LoggerFactory.getLogger(ProductCreator.class);

	private final ProductRepository productRepository;

	public ProductCreator(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Result<Product> run(String name, String description, BigDecimal price, String status) {
		var product = Product.create(name, description, price, status);
		if (product.isFailure()) {
			LOG.warn("Product creation failed: reason=domain_validation");
			return product;
		}

		var saved = productRepository.save(product.value());
		LOG.info("Product created id={}", saved.id().asString());
		return Result.success(saved);
	}
}
