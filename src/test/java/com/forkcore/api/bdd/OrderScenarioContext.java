package com.forkcore.api.bdd;

import io.cucumber.spring.ScenarioScope;
import java.net.http.HttpResponse;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class OrderScenarioContext {

	private HttpResponse<String> response;
	private String rememberedOrderId;

	public HttpResponse<String> response() {
		return response;
	}

	public void setResponse(HttpResponse<String> response) {
		this.response = response;
	}

	public String getRememberedOrderId() {
		return rememberedOrderId;
	}

	public void setRememberedOrderId(String rememberedOrderId) {
		this.rememberedOrderId = rememberedOrderId;
	}

	public void reset() {
		response = null;
		rememberedOrderId = null;
	}
}
