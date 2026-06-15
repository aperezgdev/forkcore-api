package com.forkcore.api.tables.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkcore.api.tables.application.TableCreator;
import com.forkcore.api.tables.domain.Table;
import com.forkcore.api.tables.infrastructure.out.persistence.InMemoryTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TableControllerTest {

	private final InMemoryTableRepository repository = new InMemoryTableRepository();
	private final TableController controller = new TableController(new TableCreator(repository));
	private final ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}

	@Test
	void shouldCreateTableAndReturn201WithLocation() throws Exception {
		var request = new CreateTableRequest("T-01", 4, "Terraza", "available");

		var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		var response = mockMvc.perform(
				post("/tables")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.code").value("T-01"))
			.andExpect(jsonPath("$.capacity").value(4))
			.andExpect(jsonPath("$.location").value("Terraza"))
			.andExpect(jsonPath("$.status").value("available"))
			.andReturn();

		assertThat(response.getResponse().getHeader("Location")).matches("/tables/.+");
	}

	@Test
	void shouldReturn201WithDefaultStatusWhenStatusOmitted() throws Exception {
		var request = new CreateTableRequest("T-02", 2, "Salon", null);

		var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(
				post("/tables")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("T-02"))
			.andExpect(jsonPath("$.capacity").value(2))
			.andExpect(jsonPath("$.status").value("available"));
	}

	@Test
	void shouldReturn201WithNullLocationWhenLocationOmitted() throws Exception {
		var request = new CreateTableRequest("T-03", 6, null, null);

		var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(
				post("/tables")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("T-03"))
			.andExpect(jsonPath("$.capacity").value(6))
			.andExpect(jsonPath("$.location").doesNotExist())
			.andExpect(jsonPath("$.status").value("available"));
	}

	@Test
	void shouldReturn201WithNullLocationWhenLocationIsWhitespace() throws Exception {
		var request = new CreateTableRequest("T-04", 2, "   ", null);

		var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(
				post("/tables")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("T-04"))
			.andExpect(jsonPath("$.capacity").value(2))
			.andExpect(jsonPath("$.location").doesNotExist())
			.andExpect(jsonPath("$.status").value("available"));
	}

	@Test
	void shouldReturn400WithBothErrorsWhenCodeAndCapacityAreInvalid() throws Exception {
		var request = new CreateTableRequest("", 0, null, null);

		var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(
				post("/tables")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors").isArray())
			.andExpect(jsonPath("$.errors.length()").value(2))
			.andExpect(jsonPath("$.errors[?(@.field=='code')].message").value("table code is required"))
			.andExpect(jsonPath("$.errors[?(@.field=='capacity')].message").value("table capacity must be greater than or equal to one"));
	}

	@Test
	void shouldReturn400WhenStatusIsReserved() throws Exception {
		var request = new CreateTableRequest("T-05", 2, null, "reserved");

		var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(
				post("/tables")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors").isArray())
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[0].field").value("status"))
			.andExpect(jsonPath("$.errors[0].message").value("table status is invalid"));
	}

	@Test
	void shouldReturn409WhenCodeAlreadyExists() throws Exception {
		var existing = Table.create("EXISTING", 4, "Salon", "available").value();
		repository.save(existing);

		var request = new CreateTableRequest("EXISTING", 8, "Patio", "available");

		var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(
				post("/tables")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.errors").isArray())
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[0].field").value("code"))
			.andExpect(jsonPath("$.errors[0].message").value("table code already exists"));
	}
}
