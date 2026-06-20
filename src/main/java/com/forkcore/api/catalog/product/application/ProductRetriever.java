package com.forkcore.api.catalog.product.application;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.catalog.product.domain.vo.ProductStatus;
import com.forkcore.api.shared.domain.result.Result;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductRetriever {

	private static final Logger LOG = LoggerFactory.getLogger(ProductRetriever.class);

	private final ProductRepository productRepository;

	public ProductRetriever(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Result<List<Product>> run(String status) {
		if (status == null || status.isBlank()) {
			LOG.debug("Listing all products");
			return Result.success(productRepository.findAll());
		}

		LOG.debug("Listing products by status={}", status);
		return ProductStatus.from(status)
			.map(ProductStatus::value)
			.map(productRepository::findByStatus);
	}
}
