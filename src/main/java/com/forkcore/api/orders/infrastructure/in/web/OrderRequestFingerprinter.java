package com.forkcore.api.orders.infrastructure.in.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Component;

@Component
public class OrderRequestFingerprinter {

	private static final String SHA_256 = "SHA-256";
	private final ObjectMapper objectMapper;

	public OrderRequestFingerprinter() {
		this.objectMapper = new ObjectMapper();
	}

	public String fingerprint(CreateOrderRequest request) {
		var canonical = buildCanonicalJson(request);
		return sha256Hex(canonical);
	}

	String buildCanonicalJson(CreateOrderRequest request) {
		var map = new LinkedHashMap<String, Object>();

		var lines = request.lines().stream()
			.map(line -> {
				var lineMap = new LinkedHashMap<String, Object>();
				lineMap.put("productId", line.productId());
				lineMap.put("quantity", line.quantity());
				return lineMap;
			})
			.toList();
		map.put("lines", lines);

		if (request.tableId() != null) {
			map.put("tableId", request.tableId());
		}

		if (request.notes() != null) {
			map.put("notes", request.notes());
		}

		try {
			return objectMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize canonical JSON", e);
		}
	}

	private static String sha256Hex(String input) {
		try {
			var digest = MessageDigest.getInstance(SHA_256);
			var hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			var hexString = new StringBuilder();
			for (var b : hash) {
				hexString.append(String.format("%02x", b));
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
