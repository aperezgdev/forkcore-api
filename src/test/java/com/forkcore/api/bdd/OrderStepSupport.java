package com.forkcore.api.bdd;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public abstract class OrderStepSupport {

	protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	@LocalServerPort
	protected int port;

	@Autowired
	protected OrderScenarioContext scenarioContext;

	protected void send(HttpRequest request) throws java.io.IOException, InterruptedException {
		scenarioContext.setResponse(HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()));
	}

	protected HttpResponse<String> response() {
		return scenarioContext.response();
	}

	protected String url(String path) {
		return "http://localhost:" + port + path;
	}
}
