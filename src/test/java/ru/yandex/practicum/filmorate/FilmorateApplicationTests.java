package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private User validUser;
	private Film validFilm;

	@BeforeEach
	void setUp() {
		validUser = new User();
		validUser.setLogin("admin");
		validUser.setEmail("admin@admin");
		validUser.setBirthday(LocalDate.of(2000, 1, 1));
		validUser.setName("Admin");

		validFilm = new Film();
		validFilm.setName("Test Film");
		validFilm.setDescription("Test description");
		validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
		validFilm.setDuration(120);
	}

	@Test
	void shouldCreateValidUser() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.login").value("admin"))
				.andExpect(jsonPath("$.email").value("admin@admin"))
				.andExpect(jsonPath("$.name").value("Admin"));
	}

	@Test
	void shouldFailWhenUserEmailIsNull() throws Exception {
		validUser.setEmail(null);
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validUser)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldFailWhenUserEmailIsInvalid() throws Exception {
		validUser.setEmail("invalid-email");
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validUser)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldFailWhenUserBirthdayIsInFuture() throws Exception {
		validUser.setBirthday(LocalDate.now().plusDays(1));
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validUser)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldSetNameToLoginWhenNameIsNull() throws Exception {
		validUser.setName(null);
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(validUser.getLogin()));
	}

	@Test
	void shouldCreateValidFilm() throws Exception {
		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validFilm)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value("Test Film"))
				.andExpect(jsonPath("$.description").value("Test description"))
				.andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
				.andExpect(jsonPath("$.duration").value(120));
	}

	@Test
	void shouldFailWhenFilmNameIsNull() throws Exception {
		validFilm.setName(null);
		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validFilm)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldFailWhenFilmDescriptionExceedsMaxLength() throws Exception {
		validFilm.setDescription("a".repeat(201));
		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validFilm)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldFailWhenFilmReleaseDateIsBeforeFirstFilmDate() throws Exception {
		validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validFilm)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldFailWhenFilmDurationIsNegative() throws Exception {
		validFilm.setDuration(-1);
		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validFilm)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldUpdateFilm() throws Exception {
		// Сначала создаём фильм
		String response = mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validFilm)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Film created = objectMapper.readValue(response, Film.class);
		created.setName("Updated Name");

		mockMvc.perform(put("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Name"));
	}

	@Test
	void shouldFailUpdateWhenFilmNotFound() throws Exception {
		validFilm.setId(999L);
		mockMvc.perform(put("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validFilm)))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldUpdateUser() throws Exception {
		String response = mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validUser)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		User created = objectMapper.readValue(response, User.class);
		created.setLogin("newLogin");

		mockMvc.perform(put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.login").value("newLogin"));
	}

	@Test
	void shouldFailUpdateWhenUserNotFound() throws Exception {
		validUser.setId(999L);
		mockMvc.perform(put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validUser)))
				.andExpect(status().isNotFound());
	}
}