package com.forkcore.api.orders.domain;

import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.ProductPrice;
import java.util.Optional;

public interface ProductPriceProvider {

	Optional<ProductPrice> findPrice(Id productId);
}
