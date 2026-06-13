package com.forkcore.api.catalog.product.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forkcore.api.catalog.product.application.ProductCreator;
import com.forkcore.api.catalog.product.application.ProductDeleter;
import com.forkcore.api.catalog.product.application.ProductRetriever;
import com.forkcore.api.catalog.product.application.ProductUpdater;
import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductControllerDeleteContractTest {

	private MockMvc mockMvc;
	private InMemoryRepository repository;

	@BeforeEach
	void setUp() {
		repository = new InMemoryRepository();
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository),
			new ProductDeleter(repository)
		);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.build();
	}

	@Test
	void deleteReturns404WithBodyThatIsNotProblemDetail() throws Exception {
		mockMvc.perform(delete("/products/99999999-9999-9999-9999-999999999999"))
			.andExpect(status().isNotFound())
			.andExpect(result -> {
				String contentType = result.getResponse().getContentType();
				if (contentType != null) {
					assertThat(contentType).doesNotContain(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
				}
			})
			.andExpect(content().string(""));
	}

	@Test
	void deleteReturns400WithBodyThatIsNotProblemDetail() throws Exception {
		mockMvc.perform(delete("/products/not-a-uuid"))
			.andExpect(status().isBadRequest())
			.andExpect(result -> {
				String contentType = result.getResponse().getContentType();
				if (contentType != null) {
					assertThat(contentType).doesNotContain(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
				}
			})
			.andExpect(content().string(""));
	}

	@Test
	void deleteReturns204WithEmptyBody() throws Exception {
		repository.save(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);

		mockMvc.perform(delete("/products/11111111-1111-1111-1111-111111111111"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));
	}

	private static final class InMemoryRepository implements ProductRepository {

		private final List<Product> products = new ArrayList<>();

		@Override
		public Product save(Product product) {
			products.removeIf(existing -> existing.id().equals(product.id()));
			products.add(product);
			return product;
		}

		@Override
		public List<Product> findAll() {
			return List.copyOf(products);
		}

		@Override
		public List<Product> findByStatus(String status) {
			return products.stream().filter(product -> product.status().equals(status)).toList();
		}

		@Override
		public Optional<Product> findById(Id id) {
			return products.stream().filter(product -> product.id().equals(id)).findFirst();
		}

		@Override
		public void delete(Product product) {
			products.removeIf(existing -> existing.id().equals(product.id()));
		}
	}
}
