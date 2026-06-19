package com.forkcore.api.bdd;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class OrderRegistrationSteps extends OrderStepSupport {

	@When("I create an order with a line for product {string} with quantity {int}")
	public void createOrderSingleLine(String productId, int quantity)
			throws IOException, InterruptedException {
		var body = buildSingleLineOrderBody(productId, quantity, null, null);
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with quantity {int} and table {string}")
	public void createOrderSingleLineWithTable(String productId, int quantity, String tableId)
			throws IOException, InterruptedException {
		var body = buildSingleLineOrderBody(productId, quantity, tableId, null);
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with quantity {int} and notes {string}")
	public void createOrderSingleLineWithNotes(String productId, int quantity, String notes)
			throws IOException, InterruptedException {
		var body = buildSingleLineOrderBody(productId, quantity, null, notes);
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with quantity {int}, table {string} and notes {string}")
	public void createOrderSingleLineWithTableAndNotes(String productId, int quantity, String tableId, String notes)
			throws IOException, InterruptedException {
		var body = buildSingleLineOrderBody(productId, quantity, tableId, notes);
		sendPost(body, null);
	}

	@When("I create an order with two lines: product {string} quantity {int} and product {string} quantity {int}")
	public void createOrderTwoLines(String productId1, int quantity1, String productId2, int quantity2)
			throws IOException, InterruptedException {
		var body = """
			{
			  "lines": [
			    { "productId": "%s", "quantity": %d },
			    { "productId": "%s", "quantity": %d }
			  ]
			}
			""".formatted(productId1, quantity1, productId2, quantity2);
		sendPost(body, null);
	}

	@When("I create an order with an empty lines list")
	public void createOrderWithEmptyLines()
			throws IOException, InterruptedException {
		var body = """
			{
			  "lines": []
			}
			""";
		sendPost(body, null);
	}

	@When("I create an order without a lines field")
	public void createOrderWithoutLinesField()
			throws IOException, InterruptedException {
		var body = """
			{
			  "tableId": null,
			  "notes": null
			}
			""";
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with raw quantity {string}")
	public void createOrderWithRawQuantity(String productId, String rawQuantity)
			throws IOException, InterruptedException {
		// Send the raw quantity as a JSON string so Jackson fails to parse it as Integer
		var body = """
			{
			  "lines": [
			    { "productId": "%s", "quantity": "%s" }
			  ]
			}
			""".formatted(productId, rawQuantity);
		sendPost(body, null);
	}

	@When("I create an order with a line with productId {word} and quantity {int}")
	public void createOrderWithProductIdAndQuantity(String productId, int quantity)
			throws IOException, InterruptedException {
		var body = buildSingleLineOrderBody(productId, quantity, null, null);
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with quantity {int} and a body status {string}")
	public void createOrderWithBodyStatus(String productId, int quantity, String status)
			throws IOException, InterruptedException {
		var body = """
			{
			  "lines": [
			    { "productId": "%s", "quantity": %d }
			  ],
			  "status": "%s"
			}
			""".formatted(productId, quantity, status);
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with quantity {int} and a body total {double}")
	public void createOrderWithBodyTotal(String productId, int quantity, double total)
			throws IOException, InterruptedException {
		var body = """
			{
			  "lines": [
			    { "productId": "%s", "quantity": %d }
			  ],
			  "total": %s
			}
			""".formatted(productId, quantity, formatDouble(total));
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with quantity {int} and a body line unitPrice {double}")
	public void createOrderWithBodyLineUnitPrice(String productId, int quantity, double unitPrice)
			throws IOException, InterruptedException {
		var body = """
			{
			  "lines": [
			    { "productId": "%s", "quantity": %d, "unitPrice": %s }
			  ]
			}
			""".formatted(productId, quantity, formatDouble(unitPrice));
		sendPost(body, null);
	}

	@When("I create an order with a line for product {string} with quantity {int} and Idempotency-Key {string}")
	public void createOrderWithIdempotencyKey(String productId, int quantity, String idempotencyKey)
			throws IOException, InterruptedException {
		var body = buildSingleLineOrderBody(productId, quantity, null, null);
		sendPost(body, idempotencyKey);
	}

	@When("I create an order with a line for product {string} with quantity {int} and an Idempotency-Key of length {int}")
	public void createOrderWithIdempotencyKeyOfLength(String productId, int quantity, int keyLength)
			throws IOException, InterruptedException {
		var key = "x".repeat(keyLength);
		var body = buildSingleLineOrderBody(productId, quantity, null, null);
		sendPost(body, key);
	}

	@When("I create an order with a line for product {string} with quantity {int} and a blank Idempotency-Key")
	public void createOrderWithBlankIdempotencyKey(String productId, int quantity)
			throws IOException, InterruptedException {
		var body = buildSingleLineOrderBody(productId, quantity, null, null);
		sendPost(body, "   ");
	}

	@When("I POST a malformed JSON body to \\/orders")
	public void postMalformedJsonToOrders()
			throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/orders")))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString("this is not json"))
			.build();
		send(request);
	}

	// ========== helpers ==========

	private void sendPost(String body, String idempotencyKey)
			throws IOException, InterruptedException {
		var builder = HttpRequest.newBuilder()
			.uri(URI.create(url("/orders")))
			.header("Content-Type", "application/json");

		if (idempotencyKey != null) {
			builder.header("Idempotency-Key", idempotencyKey);
		}

		var request = builder
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.build();
		send(request);
	}

	private String buildSingleLineOrderBody(String productId, int quantity, String tableId, String notes) {
		var sb = new StringBuilder();
		sb.append("{\"lines\":[{\"productId\":\"")
			.append(escapeJson(productId))
			.append("\",\"quantity\":")
			.append(quantity)
			.append("}]");

		if (tableId != null) {
			sb.append(",\"tableId\":\"")
				.append(escapeJson(tableId))
				.append("\"");
		}

		if (notes != null) {
			sb.append(",\"notes\":\"")
				.append(escapeJson(notes))
				.append("\"");
		}

		sb.append("}");
		return sb.toString();
	}

	private static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static String formatDouble(double value) {
		return java.math.BigDecimal.valueOf(value).toPlainString();
	}
}
