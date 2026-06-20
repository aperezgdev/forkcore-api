package com.forkcore.api.bdd;

import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderLine;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.shared.domain.Id;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderStatusUpdateSteps extends OrderStepSupport {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	// ========== GIVEN ==========

	@Given("an order exists in status {string} with a line for product {string} with quantity {int}")
	public void anOrderExistsInStatus(String status, String productId, int quantity) {
		var productIdResult = Id.from(productId);
		if (productIdResult.isFailure()) {
			throw new IllegalArgumentException("Invalid product id: " + productId);
		}

		var productOpt = productRepository.findById(productIdResult.value());
		BigDecimal unitPrice;
		if (productOpt.isPresent()) {
			unitPrice = productOpt.get().price();
		} else {
			unitPrice = BigDecimal.ZERO;
		}

		var line = OrderLine.fromPrimitives(
			Id.create().asString(),
			productId,
			quantity,
			unitPrice
		);

		var total = unitPrice.multiply(BigDecimal.valueOf(quantity));

		var order = Order.fromPrimitives(
			Id.create().asString(),
			status,
			List.of(line),
			null,
			null,
			total
		);

		orderRepository.save(order);
		scenarioContext.setRememberedOrderId(order.id().asString());
	}

	// ========== WHEN ==========

	@When("I PATCH the order status to {string}")
	public void patchOrderStatus(String status) throws IOException, InterruptedException {
		var body = "{\"status\":\"" + escapeJson(status) + "\"}";
		var orderId = scenarioContext.getRememberedOrderId();
		sendPatch(orderId, body);
	}

	@When("I PATCH the order status with body:")
	public void patchOrderStatusWithBody(String body) throws IOException, InterruptedException {
		var orderId = scenarioContext.getRememberedOrderId();
		sendPatch(orderId, body);
	}

	@When("I PATCH the order status with a malformed JSON body")
	public void patchOrderStatusWithMalformedBody() throws IOException, InterruptedException {
		var orderId = scenarioContext.getRememberedOrderId();
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/orders/" + orderId + "/status")))
			.header("Content-Type", "application/json")
			.method("PATCH", HttpRequest.BodyPublishers.ofString("{not valid json"))
			.build();
		send(request);
	}

	@When("I PATCH the status of a non-existent order to {string}")
	public void patchNonExistentOrderStatus(String status) throws IOException, InterruptedException {
		var body = "{\"status\":\"" + escapeJson(status) + "\"}";
		sendPatch("99999999-9999-9999-9999-999999999999", body);
	}

	@When("I PATCH the order status of id {string} to {string}")
	public void patchOrderStatusWithId(String id, String status) throws IOException, InterruptedException {
		var body = "{\"status\":\"" + escapeJson(status) + "\"}";
		sendPatch(id, body);
	}

	// ========== helpers ==========

	private void sendPatch(String orderId, String body) throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/orders/" + orderId + "/status")))
			.header("Content-Type", "application/json")
			.method("PATCH", HttpRequest.BodyPublishers.ofString(body))
			.build();
		send(request);
	}

	private static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
