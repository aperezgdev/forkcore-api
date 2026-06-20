package com.forkcore.api.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);
	private static final String REQUEST_ID_HEADER = "X-Request-Id";
	private static final String MDC_REQUEST_ID = "requestId";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		var requestId = request.getHeader(REQUEST_ID_HEADER);
		if (requestId == null || requestId.isBlank()) {
			requestId = UUID.randomUUID().toString();
		}

		MDC.put(MDC_REQUEST_ID, requestId);
		response.setHeader(REQUEST_ID_HEADER, requestId);

		var startNanos = System.nanoTime();
		try {
			filterChain.doFilter(request, response);
		} finally {
			var durationMs = (System.nanoTime() - startNanos) / 1_000_000;
			LOG.info("request method={} uri={} status={} durationMs={}",
				request.getMethod(),
				request.getRequestURI(),
				response.getStatus(),
				durationMs);
			MDC.remove(MDC_REQUEST_ID);
		}
	}
}
