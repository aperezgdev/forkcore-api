package com.forkcore.api.orders.infrastructure.out.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.catalog.product.infrastructure.out.persistence.InMemoryProductRepository;
import com.forkcore.api.shared.domain.Id;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogProductPriceProviderTest {

	private ProductRepository productRepository;
	private CatalogProductPriceProvider provider;

	@BeforeEach
	void setUp() {
		productRepository = new InMemoryProductRepository();
		provider = new CatalogProductPriceProvider(productRepository);
	}

	@Test
	void shouldReturnPriceForExistingProduct() {
		var product = Product.create("Burger", "Classic burger", new BigDecimal("10.00"), "active").value();
		productRepository.save(product);

		var result = provider.findPrice(product.id());

		assertThat(result).isPresent();
		assertThat(result.get().unitPrice()).isEqualByComparingTo("10.00");
		assertThat(result.get().productId()).isEqualTo(product.id());
	}

	@Test
	void shouldReturnEmptyForNonExistentProduct() {
		var result = provider.findPrice(Id.create());

		assertThat(result).isEmpty();
	}

	@Test
	void shouldReturnEmptyForNullPrice() {
		var id = Id.create();
		var product = Product.fromPrimitives(
			id.asString(),
			"Burger",
			"Classic burger",
			null,
			"active"
		);
		productRepository.save(product);

		var result = provider.findPrice(id);

		assertThat(result).isEmpty();
	}
}
