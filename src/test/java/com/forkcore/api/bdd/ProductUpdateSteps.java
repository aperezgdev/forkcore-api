package com.forkcore.api.bdd;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class ProductUpdateSteps extends ProductStepSupport {

	@When("I update the product with id {string} with name {string}")
	public void updateProductWithName(String id, String name) throws IOException, InterruptedException {
		sendPatchRequest(id, """
			{
			  "name": "%s"
			}
			""".formatted(name));
	}

	@When("I update the product with id {string} with name {string} and body id {string}")
	public void updateProductWithNameAndBodyId(String id, String name, String bodyId) throws IOException, InterruptedException {
		sendPatchRequest(id, """
			{
			  "id": "%s",
			  "name": "%s"
			}
			""".formatted(bodyId, name));
	}

	@When("I update the product with id {string} with null description")
	public void updateProductWithNullDescription(String id) throws IOException, InterruptedException {
		sendPatchRequest(id, """
			{
			  "description": null
			}
			""");
	}

	@When("I update the product with id {string} with description {string}")
	public void updateProductWithDescription(String id, String description) throws IOException, InterruptedException {
		sendPatchRequest(id, """
			{
			  "description": "%s"
			}
			""".formatted(description));
	}

	@When("I update the product with id {string} with price {double}")
	public void updateProductWithPrice(String id, double price) throws IOException, InterruptedException {
		sendPatchRequest(id, """
			{
			  "price": %s
			}
			""".formatted(price));
	}

	@When("I update the product with id {string} with status {string}")
	public void updateProductWithStatus(String id, String status) throws IOException, InterruptedException {
		sendPatchRequest(id, """
			{
			  "status": "%s"
			}
			""".formatted(status));
	}

	private void sendPatchRequest(String id, String body) throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/products/" + id)))
			.header("Content-Type", "application/json")
			.method("PATCH", HttpRequest.BodyPublishers.ofString(body))
			.build();

		send(request);
	}
}
