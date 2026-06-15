package com.forkcore.api.bdd;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class TableDeletionSteps extends TableStepSupport {

	@When("I delete the table with id {string}")
	public void deleteTable(String id) throws IOException, InterruptedException {
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url("/tables/" + id)))
			.DELETE()
			.build();

		send(request);
	}
}
