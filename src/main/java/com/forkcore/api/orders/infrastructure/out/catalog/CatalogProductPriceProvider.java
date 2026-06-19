package com.forkcore.api.orders.infrastructure.out.catalog;

import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.orders.domain.ProductPriceProvider;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.ProductPrice;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CatalogProductPriceProvider implements ProductPriceProvider {

	private final ProductRepository productRepository;

	public CatalogProductPriceProvider(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public Optional<ProductPrice> findPrice(Id productId) {
		return productRepository.findById(productId)
			.filter(product -> product.price() != null && product.price().signum() >= 0)
			.map(product -> new ProductPrice(productId, product.price()));
	}
}
