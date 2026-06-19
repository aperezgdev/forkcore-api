package com.forkcore.api.bdd;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.vo.ProductDescription;
import com.forkcore.api.catalog.product.domain.vo.ProductName;
import com.forkcore.api.catalog.product.domain.vo.ProductStatus;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.ProductPrice;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public abstract class ProductStepSupport {

	protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	@LocalServerPort
	protected int port;

	@Autowired
	protected ProductRepository productRepository;

	@Autowired
	protected ProductScenarioContext scenarioContext;

	protected void saveProduct(String name, String description, double price, String status) {
		productRepository.save(Product.create(name, description, BigDecimal.valueOf(price), status).value());
	}

	protected void saveProduct(String id, String name, String description, double price, String status) {
		productRepository.save(createProductWithId(id, name, description, price, status));
	}

	protected void send(HttpRequest request) throws java.io.IOException, InterruptedException {
		scenarioContext.setResponse(HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()));
	}

	protected HttpResponse<String> response() {
		return scenarioContext.response();
	}

	protected String url(String path) {
		return "http://localhost:" + port + path;
	}

	private Product createProductWithId(String id, String name, String description, double price, String status) {
		try {
			var constructor = Product.class.getDeclaredConstructor(
				Id.class,
				ProductName.class,
				ProductDescription.class,
				ProductPrice.class,
				ProductStatus.class
			);
			constructor.setAccessible(true);

			return constructor.newInstance(
				Id.fromStringOrThrow(id),
				validatedName(name),
				ProductDescription.from(description),
				validatedPrice(price),
				validatedStatus(status)
			);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
			throw new IllegalStateException("Unable to create product for BDD scenario", exception);
		}
	}

	private ProductName validatedName(String value) {
		var result = ProductName.from(value);
		if (result.isFailure()) {
			throw new IllegalArgumentException(result.error().message());
		}

		return result.value();
	}

	private ProductPrice validatedPrice(double value) {
		var result = ProductPrice.from(BigDecimal.valueOf(value));
		if (result.isFailure()) {
			throw new IllegalArgumentException(result.error().message());
		}

		return result.value();
	}

	private ProductStatus validatedStatus(String value) {
		var result = ProductStatus.from(value);
		if (result.isFailure()) {
			throw new IllegalArgumentException(result.error().message());
		}

		return result.value();
	}
}
