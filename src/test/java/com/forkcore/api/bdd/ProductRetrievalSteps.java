package com.forkcore.api.bdd;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class ProductRetrievalSteps extends ProductStepSupport {

	@When("I retrieve products without filters")
	public void retrieveProductsWithoutFilters() throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder().uri(URI.create(url("/products"))).GET().build();
		send(request);
	}

	@When("I retrieve products filtered by status {string}")
	public void retrieveProductsFilteredByStatus(String status) throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder().uri(URI.create(url("/products?status=" + status))).GET().build();
		send(request);
	}
}
