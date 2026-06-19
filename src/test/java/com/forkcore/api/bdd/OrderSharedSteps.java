package com.forkcore.api.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.catalog.product.domain.vo.ProductDescription;
import com.forkcore.api.catalog.product.domain.vo.ProductName;
import com.forkcore.api.catalog.product.domain.vo.ProductStatus;
import com.forkcore.api.orders.infrastructure.out.idempotency.InMemoryIdempotencyKeyStore;
import com.forkcore.api.orders.infrastructure.out.persistence.OrderTestDataCleaner;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.ProductPrice;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class OrderSharedSteps extends OrderStepSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderTestDataCleaner orderTestDataCleaner;

	@Autowired
	private InMemoryIdempotencyKeyStore idempotencyKeyStore;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Before
	public void cleanOrderState() {
		orderTestDataCleaner.deleteAll();
		idempotencyKeyStore.clear();
		scenarioContext.reset();
		OrderBddTestConfiguration.reset();
	}

	// ========== GIVEN ==========

	@Given("a product exists with id {string} and no resolvable price")
	public void aProductExistsWithNoResolvablePrice(String id) {
		// Register this product as unresolvable so the price provider returns empty.
		OrderBddTestConfiguration.addUnresolvableId(id);
		// Create the product in the repository so it "exists" in the catalog.
		// Use a valid price since the DB CHECK constraint requires it.
		try {
			var constructor = Product.class.getDeclaredConstructor(
				Id.class,
				ProductName.class,
				ProductDescription.class,
				ProductPrice.class,
				ProductStatus.class
			);
			constructor.setAccessible(true);

			var product = constructor.newInstance(
				Id.fromStringOrThrow(id),
				ProductName.from("Unpriced Product").value(),
				ProductDescription.from("Product with no resolvable price"),
				new ProductPrice(Id.fromStringOrThrow(id), BigDecimal.valueOf(0.00)),
				ProductStatus.from("active").value()
			);
			productRepository.save(product);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to create product for BDD scenario", e);
		}
	}

	@Given("the product price provider will fail with an unexpected exception")
	public void theProductPriceProviderWillFail() {
		OrderBddTestConfiguration.setFailing(true);
	}

	// ========== THEN ==========

	@Then("the order response status code should be {int}")
	public void theOrderResponseStatusCodeShouldBe(int statusCode) {
		Assertions.assertThat(response().statusCode()).isEqualTo(statusCode);
	}

	@Then("the order response body should be empty")
	public void theOrderResponseBodyShouldBeEmpty() {
		var body = response().body();
		Assertions.assertThat(body).isNullOrEmpty();
	}

	@Then("the order response should have a Location header")
	public void theOrderResponseShouldHaveALocationHeader() {
		var location = response().headers().firstValue("Location");
		Assertions.assertThat(location).isPresent();
	}

	@Then("the order response should have a Location header for the remembered order id")
	public void theOrderResponseShouldHaveALocationHeaderForTheRememberedOrderId() {
		var location = response().headers().firstValue("Location");
		Assertions.assertThat(location).isPresent();
		var path = location.get();
		Assertions.assertThat(path).endsWith("/" + scenarioContext.getRememberedOrderId());
	}

	@Then("the order response should contain a line for product {string} with quantity {int} and unitPrice {double}")
	public void theOrderResponseShouldContainLineWithProductQuantityAndUnitPrice(
			String productId, int quantity, double unitPrice) {
		var body = response().body();
		Assertions.assertThat(body).contains("\"productId\":\"" + productId + "\"");
		Assertions.assertThat(body).contains("\"quantity\":" + quantity);
		Assertions.assertThat(body).contains("\"unitPrice\":" + unitPrice);
	}

	@Then("the order response should contain a line for product {string} with quantity {int}")
	public void theOrderResponseShouldContainLineWithProductAndQuantity(String productId, int quantity) {
		var body = response().body();
		Assertions.assertThat(body).contains("\"productId\":\"" + productId + "\"");
		Assertions.assertThat(body).contains("\"quantity\":" + quantity);
	}

	@Then("the order response should contain total {double}")
	public void theOrderResponseShouldContainTotal(double total) {
		Assertions.assertThat(response().body()).contains("\"total\":" + total);
	}

	@Then("the order response should contain status {string}")
	public void theOrderResponseShouldContainStatus(String status) {
		Assertions.assertThat(response().body()).contains("\"status\":\"" + status + "\"");
	}

	@Then("the order response should contain tableId {string}")
	public void theOrderResponseShouldContainTableId(String tableId) {
		Assertions.assertThat(response().body()).contains("\"tableId\":\"" + tableId + "\"");
	}

	@Then("the order response should contain null tableId")
	public void theOrderResponseShouldContainNullTableId() {
		Assertions.assertThat(response().body()).contains("\"tableId\":null");
	}

	@Then("the order response should contain notes {string}")
	public void theOrderResponseShouldContainNotes(String notes) {
		Assertions.assertThat(response().body()).contains("\"notes\":\"" + notes + "\"");
	}

	@Then("the order response should contain null notes")
	public void theOrderResponseShouldContainNullNotes() {
		Assertions.assertThat(response().body()).contains("\"notes\":null");
	}

	@Then("the order response should contain the remembered order id")
	public void theOrderResponseShouldContainTheRememberedOrderId() {
		Assertions.assertThat(response().body()).contains("\"id\":\"" + scenarioContext.getRememberedOrderId() + "\"");
	}

	@Then("the order response should contain a different order id from the remembered one")
	public void theOrderResponseShouldContainADifferentOrderId() {
		var body = response().body();
		var rememberedId = scenarioContext.getRememberedOrderId();
		Assertions.assertThat(body).doesNotContain("\"id\":\"" + rememberedId + "\"");
	}

	@Then("only one order should be persisted")
	public void onlyOneOrderShouldBePersisted() {
		var count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
		Assertions.assertThat(count).isEqualTo(1L);
	}

	@Then("two orders should be persisted")
	public void twoOrdersShouldBePersisted() {
		var count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
		Assertions.assertThat(count).isEqualTo(2L);
	}

	@Then("I remember the order id from the response")
	public void iRememberTheOrderIdFromTheResponse() {
		var body = response().body();
		try {
			var json = MAPPER.readTree(body);
			var id = json.get("id").asText();
			scenarioContext.setRememberedOrderId(id);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to parse order id from response", e);
		}
	}
}
