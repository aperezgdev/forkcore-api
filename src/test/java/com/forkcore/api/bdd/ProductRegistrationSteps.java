package com.forkcore.api.bdd;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class ProductRegistrationSteps extends ProductStepSupport {

	@When("I create a product with name {string}, description {string}, price {double} and no status")
	public void createProductWithoutStatus(String name, String description, double price) throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/products")))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString("""
			{
			  "name": "%s",
			  "description": "%s",
			  "price": %s
			}
			""".formatted(name, description, price)))
			.build();

		send(request);
	}
}
