package com.forkcore.api.bdd;

import io.cucumber.spring.ScenarioScope;
import java.net.http.HttpResponse;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class ProductScenarioContext {

	private HttpResponse<String> response;

	public HttpResponse<String> response() {
		return response;
	}

	public void setResponse(HttpResponse<String> response) {
		this.response = response;
	}
}
