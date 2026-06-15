package com.forkcore.api.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkcore.api.tables.infrastructure.out.persistence.TableTestDataCleaner;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.stream.StreamSupport;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

public class TableSharedSteps extends TableStepSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private TableTestDataCleaner tableTestDataCleaner;

	@Before
	public void cleanRepository() {
		tableTestDataCleaner.deleteAll();
	}

	@Given("a table exists with code {string}, capacity {int}, location {string} and status {string}")
	public void aTableExists(String code, int capacity, String location, String status) {
		saveTable(code, capacity, location, status);
	}

	@Given("a table exists with id {string}, code {string}, capacity {int}, location {string} and status {string}")
	public void aTableExistsWithId(String id, String code, int capacity, String location, String status) {
		saveTableWithId(id, code, capacity, location, status);
	}

	@Then("the table response status code should be {int}")
	public void responseStatusCodeShouldBe(int statusCode) {
		Assertions.assertThat(response().statusCode()).isEqualTo(statusCode);
	}

	@Then("the table response should be a single object")
	public void responseShouldBeSingleObject() throws Exception {
		var body = response().body();
		Assertions.assertThat(body).isNotBlank();
		var json = MAPPER.readTree(body);
		Assertions.assertThat(json.isObject()).isTrue();
	}

	@Then("the table response should contain code {string}")
	public void responseShouldContainCode(String code) {
		Assertions.assertThat(response().body()).contains("\"code\":\"" + code + "\"");
	}

	@Then("the table response should contain capacity {int}")
	public void responseShouldContainCapacity(int capacity) {
		Assertions.assertThat(response().body()).contains("\"capacity\":" + capacity);
	}

	@Then("the table response should contain location {string}")
	public void responseShouldContainLocation(String location) {
		Assertions.assertThat(response().body()).contains("\"location\":\"" + location + "\"");
	}

	@Then("the table response should contain status {string}")
	public void responseShouldContainStatus(String status) {
		Assertions.assertThat(response().body()).contains("\"status\":\"" + status + "\"");
	}

	@Then("the table response body should be empty")
	public void responseBodyShouldBeEmpty() throws Exception {
		var body = response().body();
		if (body == null || body.isBlank()) {
			return;
		}
		// The body may contain validation errors but must not contain
		// table resource fields (id, code, capacity, location, status).
		var json = MAPPER.readTree(body);
		Assertions.assertThat(json.has("id")).as("body should not contain table id").isFalse();
		Assertions.assertThat(json.has("code")).as("body should not contain top-level code").isFalse();
		Assertions.assertThat(json.has("capacity")).as("body should not contain capacity").isFalse();
		Assertions.assertThat(json.has("location")).as("body should not contain location").isFalse();
		Assertions.assertThat(json.has("status")).as("body should not contain status").isFalse();
	}

	@Then("the table response body should not be empty")
	public void responseBodyShouldNotBeEmpty() {
		Assertions.assertThat(response().body()).isNotBlank();
	}

	@Then("the table response should contain a code error")
	public void responseShouldContainCodeError() throws Exception {
		assertResponseContainsErrorField("code");
	}

	@Then("the table response should contain a capacity error")
	public void responseShouldContainCapacityError() throws Exception {
		assertResponseContainsErrorField("capacity");
	}

	@Then("the table response should contain a code already exists error")
	public void responseShouldContainCodeAlreadyExistsError() throws Exception {
		var json = MAPPER.readTree(response().body());
		var errors = json.get("errors");
		Assertions.assertThat(errors).isNotNull();
		Assertions.assertThat(errors.isArray()).isTrue();

		var codeErrorFound = StreamSupport.stream(errors.spliterator(), false)
			.filter(e -> "code".equals(e.get("field").asText()))
			.findFirst();

		Assertions.assertThat(codeErrorFound).isPresent();
		Assertions.assertThat(codeErrorFound.get().get("message").asText())
			.contains("already exists");
	}

	private void assertResponseContainsErrorField(String field) throws Exception {
		var json = MAPPER.readTree(response().body());
		var errors = json.get("errors");
		Assertions.assertThat(errors).isNotNull();
		Assertions.assertThat(errors.isArray()).isTrue();

		var fields = StreamSupport.stream(errors.spliterator(), false)
			.map(e -> e.get("field").asText())
			.toList();

		Assertions.assertThat(fields).contains(field);
	}
}
