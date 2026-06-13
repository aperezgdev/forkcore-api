package com.forkcore.api.bdd;

import com.forkcore.api.catalog.product.infrastructure.out.persistence.ProductTestDataCleaner;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;

import org.springframework.beans.factory.annotation.Autowired;

public class ProductSharedSteps extends ProductStepSupport {

	@Autowired
	private ProductTestDataCleaner productTestDataCleaner;

	@Before
	public void cleanRepository() {
		productTestDataCleaner.deleteAll();
	}

	@Given("a product exists with name {string}, description {string}, price {double} and status {string}")
	public void aProductExists(String name, String description, double price, String status) {
		saveProduct(name, description, price, status);
	}

	@Given("a product exists with id {string}, name {string}, description {string}, price {double} and status {string}")
	public void aProductExistsWithId(String id, String name, String description, double price, String status) {
		saveProduct(id, name, description, price, status);
	}

	@Then("the product response status code should be {int}")
	public void responseStatusCodeShouldBe(int statusCode) {
		Assertions.assertThat(response().statusCode()).isEqualTo(statusCode);
	}

	@Then("the product response should contain id {string}")
	public void responseShouldContainId(String id) {
		Assertions.assertThat(response().body()).contains("\"id\":\"" + id + "\"");
	}

	@Then("the product response should contain name {string}")
	public void responseShouldContainName(String name) {
		Assertions.assertThat(response().body()).contains("\"name\":\"" + name + "\"");
	}

	@Then("the product response should contain description {string}")
	public void responseShouldContainDescription(String description) {
		Assertions.assertThat(response().body()).contains("\"description\":\"" + description + "\"");
	}

	@Then("the product response should contain null description")
	public void responseShouldContainNullDescription() {
		Assertions.assertThat(response().body()).contains("\"description\":null");
	}

	@Then("the product response should contain price {double}")
	public void responseShouldContainPrice(double price) {
		Assertions.assertThat(response().body()).contains("\"price\":" + price);
	}

	@Then("the product response should contain status {string}")
	public void responseShouldContainStatus(String status) {
		Assertions.assertThat(response().body()).contains("\"status\":\"" + status + "\"");
	}

	@Then("the product list response should contain {int} products")
	public void productListShouldContainProducts(int size) {
		Assertions.assertThat(countProductsInListResponse()).isEqualTo(size);
	}

	@Then("the product list response should contain product name {string}")
	public void productListShouldContainProductName(String name) {
		Assertions.assertThat(response().body()).contains("\"name\":\"" + name + "\"");
	}

	@Then("the product list response should be empty")
	public void productListShouldBeEmpty() {
		Assertions.assertThat(response().body()).isEqualTo("[]");
	}

	@Then("the product response body should be empty")
	public void responseBodyShouldBeEmpty() {
		Assertions.assertThat(response().body()).isNullOrEmpty();
	}

	private int countProductsInListResponse() {
		return response().body().split("\"id\":", -1).length - 1;
	}
}
