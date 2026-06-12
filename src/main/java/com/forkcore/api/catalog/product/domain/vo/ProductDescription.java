package com.forkcore.api.catalog.product.domain.vo;

public record ProductDescription(String value) {

	/**
	 * Builds a {@link ProductDescription} from a raw string.
	 * <p>
	 * The factory preserves {@code null} as a valid input because the PATCH endpoint allows
	 * explicitly clearing the description. When the input is not {@code null}, leading and
	 * trailing whitespace is trimmed.
	 *
	 * @param value the raw description value, may be {@code null}.
	 * @return a new {@link ProductDescription} holding the trimmed value, or {@code null} when the input is {@code null}.
	 */
	public static ProductDescription from(String value) {
		return value == null ? null : new ProductDescription(value.trim());
	}
}
