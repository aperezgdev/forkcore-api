package com.forkcore.api.shared.observability;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RequestLoggingFilterConfig {

	@Bean
	public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
		var registration = new FilterRegistrationBean<>(new RequestLoggingFilter());
		registration.addUrlPatterns("/*");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
		return registration;
	}
}
