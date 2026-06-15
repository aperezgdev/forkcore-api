package com.forkcore.api.bdd;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class TableRegistrationSteps extends TableStepSupport {

	@When("I create a table with code {string}, capacity {int}, location {string} and status {string}")
	public void createTableWithAllFields(String code, int capacity, String location, String status)
			throws IOException, InterruptedException {
		sendCreateTableRequest(code, capacity, location, status);
	}

	@When("I create a table with code {string}, capacity {int}, location {string} and no status")
	public void createTableWithLocationNoStatus(String code, int capacity, String location)
			throws IOException, InterruptedException {
		sendCreateTableRequest(code, capacity, location, null);
	}

	@When("I create a table with code {string}, capacity {int}, no location and status {string}")
	public void createTableWithNoLocationStatus(String code, int capacity, String status)
			throws IOException, InterruptedException {
		sendCreateTableRequest(code, capacity, null, status);
	}

	@When("I create a table with code {string}, capacity {int}, no location and no status")
	public void createTableWithNoLocationNoStatus(String code, int capacity)
			throws IOException, InterruptedException {
		sendCreateTableRequest(code, capacity, null, null);
	}

	private void sendCreateTableRequest(String code, int capacity, String location, String status)
			throws IOException, InterruptedException {
		var json = buildJson(code, capacity, location, status);
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/tables")))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.build();

		send(request);
	}

	private String buildJson(String code, int capacity, String location, String status) {
		var sb = new StringBuilder();
		sb.append("{\"code\":\"")
			.append(escapeJson(code))
			.append("\",\"capacity\":")
			.append(capacity);

		if (location != null) {
			sb.append(",\"location\":\"")
				.append(escapeJson(location))
				.append("\"");
		}

		if (status != null) {
			sb.append(",\"status\":\"")
				.append(escapeJson(status))
				.append("\"");
		}

		sb.append("}");
		return sb.toString();
	}

	private String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
