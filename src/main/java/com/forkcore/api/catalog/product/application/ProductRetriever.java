package com.forkcore.api.catalog.product.application;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.catalog.product.domain.vo.ProductStatus;
import com.forkcore.api.shared.domain.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductRetriever {

	private final ProductRepository productRepository;

	public ProductRetriever(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Result<List<Product>> run(String status) {
		if (status == null || status.isBlank()) {
			return Result.success(productRepository.findAll());
		}

		return ProductStatus.from(status)
			.map(ProductStatus::value)
			.map(productRepository::findByStatus);
	}
}
