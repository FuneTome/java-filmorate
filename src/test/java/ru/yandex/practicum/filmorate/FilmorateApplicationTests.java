package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
	UserController userController;
	FilmController filmController;
	User user;
	Film film;

	@Test
	void contextLoads() {

	}

	@BeforeEach
	void initUser() {
		userController = new UserController();
		user = new User();
		user.setLogin("admin");
		user.setEmail("admin@admin");
		user.setBirthday(LocalDate.of(2020, 12, 12));
	}

	@BeforeEach
	void initFilm() {
		filmController = new FilmController();
		film = new Film();
		film.setName("a");
		film.setDescription("a");
		film.setReleaseDate(LocalDate.of(2020, 12, 12));
		film.setDuration(120);
	}

	@Test
	void checkRequiredFields() {
		user.setEmail(null);
		assertFalse(userController.isValid(user));

		user.setEmail("admin@admin");
		assertTrue(userController.isValid(user));
	}

	@Test
	void checkValidEmail() {
		user.setEmail("admin");
		assertFalse(userController.isValid(user));

		user.setEmail("admin@admin");
		assertTrue(userController.isValid(user));
	}

	@Test
	void checkInvalidBirthday() {
		user.setBirthday(LocalDate.now().plusDays(1));
		assertFalse(userController.isValid(user));
	}

	@Test
	void checkFilmInvalidField() {
		assertTrue(filmController.isValid(film));

		film.setName(null);
		assertFalse(filmController.isValid(film));
	}

	@Test
	void checkFilmInvalidName() {
		film.setName(null);
		assertFalse(filmController.isValid(film));
	}

	@Test
	void checkFilmInvalidDescription() {
		film.setDescription("a".repeat(201));
		assertFalse(filmController.isValid(film));
	}

	@Test
	void checkFilmInvalidReleaseDate() {
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		assertFalse(filmController.isValid(film));
	}

	@Test
	void checkFilmInvalidDuration() {
		film.setDuration(-1);
		assertFalse(filmController.isValid(film));
	}


}
