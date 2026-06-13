package com.forkcore.api.bdd;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class ProductDeletionSteps extends ProductStepSupport {

	@When("I delete the product with id {string}")
	public void deleteProduct(String id) throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/products/" + id)))
			.DELETE()
			.build();

		send(request);
	}
}
