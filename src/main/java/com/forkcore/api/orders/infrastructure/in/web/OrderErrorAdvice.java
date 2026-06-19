package com.forkcore.api.orders.infrastructure.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = OrderController.class)
public class OrderErrorAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(OrderErrorAdvice.class);

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Void> handleMessageNotReadable(HttpMessageNotReadableException exception) {
		LOG.warn("Malformed request body in orders controller", exception);
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Void> handleUncaughtException(Exception exception) {
		LOG.error("Unhandled exception in orders controller", exception);
		return ResponseEntity.internalServerError().build();
	}
}
