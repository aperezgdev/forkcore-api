package com.forkcore.api.catalog.product.infrastructure.in.web;

import java.math.BigDecimal;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Request body for {@code PATCH /products/{id}}.
 * <p>
 * Each field is wrapped in a {@link JsonNullable} so the controller can
 * distinguish between:
 * <ul>
 *   <li>field omitted from the request (no change applied).</li>
 *   <li>field present with a {@code null} value (explicitly cleared, only meaningful for description).</li>
 *   <li>field present with a non-null value (validated and applied).</li>
 * </ul>
 * The {@code id} from the body is intentionally ignored; the path variable is the
 * single source of truth for the target product.
 */
public record UpdateProductRequest(
	JsonNullable<String> name,
	JsonNullable<String> description,
	JsonNullable<BigDecimal> price,
	JsonNullable<String> status
) {
}
