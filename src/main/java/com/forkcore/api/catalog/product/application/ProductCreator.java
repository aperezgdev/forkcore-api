package com.forkcore.api.catalog.product.application;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ProductCreator {

	private final ProductRepository productRepository;

	public ProductCreator(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Result<Product> run(String name, String description, BigDecimal price, String status) {
		var product = Product.create(name, description, price, status);
		if (product.isFailure()) {
			return product;
		}

		return Result.success(productRepository.save(product.value()));
	}
}
