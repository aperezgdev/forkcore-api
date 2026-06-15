package com.forkcore.api.bdd;

import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.domain.TableRepository;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public abstract class TableStepSupport {

	protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	@LocalServerPort
	protected int port;

	@Autowired
	protected TableRepository tableRepository;

	@Autowired
	protected TableScenarioContext scenarioContext;

	protected void send(HttpRequest request) throws java.io.IOException, InterruptedException {
		scenarioContext.setResponse(HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()));
	}

	protected HttpResponse<String> response() {
		return scenarioContext.response();
	}

	protected String url(String path) {
		return "http://localhost:" + port + path;
	}

	protected void saveTable(String code, int capacity, String location, String status) {
		var result = Table.create(code, capacity, location, status);
		tableRepository.save(result.value());
	}
}
