package com.forkcore.api.bdd;

import com.forkcore.api.orders.domain.ProductPriceProvider;
import com.forkcore.api.orders.infrastructure.out.catalog.CatalogProductPriceProvider;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.ProductPrice;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Static holder for test flags that control the ProductPriceProvider behavior.
 * Uses static fields so they are accessible from the HTTP handler thread
 * (which is different from the Cucumber scenario scope thread).
 */
@TestConfiguration
public class OrderBddTestConfiguration {

	private static final AtomicBoolean FAILING = new AtomicBoolean(false);
	private static final Set<String> UNRESOLVABLE_IDS = new CopyOnWriteArraySet<>();

	public static void setFailing(boolean failing) {
		FAILING.set(failing);
	}

	public static void addUnresolvableId(String id) {
		UNRESOLVABLE_IDS.add(id);
	}

	public static void reset() {
		FAILING.set(false);
		UNRESOLVABLE_IDS.clear();
	}

	@Bean
	@Primary
	public ProductPriceProvider testableProductPriceProvider(
			CatalogProductPriceProvider delegate
	) {
		return new ProductPriceProvider() {
			@Override
			public Optional<ProductPrice> findPrice(Id productId) {
				if (FAILING.get()) {
					throw new RuntimeException("Simulated infrastructure failure");
				}

				if (UNRESOLVABLE_IDS.contains(productId.asString())) {
					return Optional.empty();
				}

				return delegate.findPrice(productId);
			}
		};
	}
}
