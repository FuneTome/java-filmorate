package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FilmorateApplicationTests {

	@Autowired
	private UserDbStorage userStorage;
	@Autowired
	private FilmDbStorage filmStorage;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanUp() {
		jdbcTemplate.execute("DELETE FROM Film_like");
		jdbcTemplate.execute("DELETE FROM Film_genre");
		jdbcTemplate.execute("DELETE FROM Friendship");
		jdbcTemplate.execute("DELETE FROM Film");
		jdbcTemplate.execute("DELETE FROM Users");
		jdbcTemplate.execute("ALTER TABLE Film ALTER COLUMN film_id RESTART WITH 1");
		jdbcTemplate.execute("ALTER TABLE Users ALTER COLUMN user_id RESTART WITH 1");
	}

	@Test
	void addUserShouldSaveAndReturnUserWithId() {
		User user = new User();
		user.setEmail("user@ya.ru");
		user.setLogin("login");
		user.setName("name");
		user.setBirthday(LocalDate.of(2000, 1, 1));

		User saved = userStorage.addUser(user);

		assertThat(saved.getId()).isPositive();
		assertThat(saved.getEmail()).isEqualTo("user@ya.ru");
		assertThat(saved.getLogin()).isEqualTo("login");
	}

	@Test
	void updateUserShouldChangeFields() {
		User oldUser = userStorage.addUser(makeUser("old@ya.ru", "old", "Old", LocalDate.of(1990, 5, 5)));
		User newData = makeUser("new@ya.ru", "new", "New", LocalDate.of(2000, 1, 1));

		User updated = userStorage.updateUser(oldUser, newData);

		assertThat(updated.getId()).isEqualTo(oldUser.getId());
		assertThat(updated.getEmail()).isEqualTo("new@ya.ru");
		assertThat(updated.getLogin()).isEqualTo("new");
		assertThat(updated.getName()).isEqualTo("New");
	}

	@Test
	void findUserByIdShouldReturnTrueForExistingUserAndFalseForMissing() {
		User user = userStorage.addUser(makeUser("1@ya.ru", "1", "name", LocalDate.now()));
		assertThat(userStorage.findById(user.getId())).isTrue();
		assertThat(userStorage.findById(999L)).isFalse();
	}

	@Test
	void getByIdShouldReturnUserOrThrowException() {
		User user = userStorage.addUser(makeUser("2@ya.ru", "2", "name", LocalDate.now()));
		User found = userStorage.getById(user.getId());
		assertThat(found.getEmail()).isEqualTo("2@ya.ru");

		assertThatThrownBy(() -> userStorage.getById(999L))
				.isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void getUsersShouldReturnAllAddedUsers() {
		userStorage.addUser(makeUser("a@ya.ru", "a", "A", LocalDate.now()));
		userStorage.addUser(makeUser("b@ya.ru", "b", "B", LocalDate.now()));

		Map<Long, User> users = userStorage.getUsers();
		assertThat(users).hasSize(2);
	}

	@Test
	void addFilmShouldSaveFilmWithGenres() {
		Film film = makeFilm("Film", 120, 1);
		film.setGenres(new HashSet<>(Arrays.asList(new Genre(1, null)))); // только id, имя не важно

		Film saved = filmStorage.addFilm(film);

		assertThat(saved.getId()).isPositive();
		assertThat(saved.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1);
	}

	@Test
	void updateFilmShouldReplaceAllDataAndGenres() {
		Film oldFilm = filmStorage.addFilm(makeFilm("Old", 90, 1));
		oldFilm.getGenres().add(new Genre(1, null));

		Film newData = makeFilm("New", 150, 3);
		newData.setGenres(new HashSet<>(Arrays.asList(new Genre(2, null), new Genre(3, null))));

		Film updated = filmStorage.updateFilm(oldFilm, newData);

		assertThat(updated.getId()).isEqualTo(oldFilm.getId());
		assertThat(updated.getName()).isEqualTo("New");
		assertThat(updated.getRating().getId()).isEqualTo(3);
		assertThat(updated.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(2, 3);
	}

	@Test
	void findFilmByIdShouldReturnTrueForExistingFilmAndFalseForMissing() {
		Film film = filmStorage.addFilm(makeFilm("F", 100, 1));
		assertThat(filmStorage.findById(film.getId())).isTrue();
		assertThat(filmStorage.findById(999L)).isFalse();
	}

	@Test
	void getFilmByIdShouldReturnFilmWithGenres() {
		Film film = filmStorage.addFilm(makeFilm("Fav", 110, 2));
		film.getGenres().add(new Genre(4, null));
		filmStorage.updateFilm(film, film);

		User user = userStorage.addUser(makeUser("fan@ya.ru", "fan", "fan", LocalDate.now()));
		filmStorage.addLike(film.getId(), user.getId());

		Film found = filmStorage.getById(film.getId());
		assertThat(found.getName()).isEqualTo("Fav");
		assertThat(found.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(4);
		List<Long> likes = jdbcTemplate.queryForList("SELECT user_id FROM film_like WHERE film_id = ?", Long.class, film.getId());
		assertThat(likes).containsExactlyInAnyOrder(user.getId());
	}

	@Test
	void getFilmsShouldReturnAllFilms() {
		filmStorage.addFilm(makeFilm("One", 80, 1));
		filmStorage.addFilm(makeFilm("Two", 90, 2));

		Map<Long, Film> films = filmStorage.getFilms();
		assertThat(films).hasSize(2);
	}

	@Test
	void addLikeShouldPersistAndReflectInDb() {
		User user = userStorage.addUser(makeUser("like@ya.ru", "liker", "Liker", LocalDate.now()));
		Film film = filmStorage.addFilm(makeFilm("Liked", 100, 1));

		boolean added = filmStorage.addLike(film.getId(), user.getId());
		assertThat(added).isTrue();

		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM film_like WHERE film_id = ? AND user_id = ?", Integer.class, film.getId(), user.getId());
		assertThat(count).isEqualTo(1);
	}

	@Test
	void removeLikeShouldDeleteFromDb() {
		User user = userStorage.addUser(makeUser("unlike@ya.ru", "unliker", "Unlike", LocalDate.now()));
		Film film = filmStorage.addFilm(makeFilm("Dislike", 100, 1));
		filmStorage.addLike(film.getId(), user.getId());

		boolean removed = filmStorage.removeLike(film.getId(), user.getId());
		assertThat(removed).isTrue();

		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM film_like WHERE film_id = ? AND user_id = ?", Integer.class, film.getId(), user.getId());
		assertThat(count).isEqualTo(0);
	}

	@Test
	void addFriendShouldPersistAndAppearInUser() {
		User user1 = userStorage.addUser(makeUser("a@ya.ru", "a", "A", LocalDate.now()));
		User user2 = userStorage.addUser(makeUser("b@ya.ru", "b", "B", LocalDate.now()));

		userStorage.addFriend(user1.getId(), user2.getId());

		List<Long> friendsOfUser1 = jdbcTemplate.queryForList(
				"SELECT friend_id FROM friendship WHERE user_id = ? AND friendship_status_id = 2", Long.class, user1.getId());
		assertThat(friendsOfUser1).containsExactlyInAnyOrder(user2.getId());

		List<Long> friendsOfUser2 = jdbcTemplate.queryForList(
				"SELECT friend_id FROM friendship WHERE user_id = ? AND friendship_status_id = 2", Long.class, user2.getId());
		assertThat(friendsOfUser2).isEmpty();
	}

	@Test
	void removeFriendShouldDeleteOnlyForInitiator() {
		User user1 = userStorage.addUser(makeUser("x@ya.ru", "x", "X", LocalDate.now()));
		User user2 = userStorage.addUser(makeUser("y@ya.ru", "y", "Y", LocalDate.now()));
		userStorage.addFriend(user1.getId(), user2.getId());

		boolean removed = userStorage.removeFriend(user1.getId(), user2.getId());
		assertThat(removed).isTrue();

		List<Long> friendsOfUser1 = jdbcTemplate.queryForList(
				"SELECT friend_id FROM friendship WHERE user_id = ? AND friendship_status_id = 2", Long.class, user1.getId());
		assertThat(friendsOfUser1).isEmpty();

		List<Long> friendsOfUser2 = jdbcTemplate.queryForList(
				"SELECT friend_id FROM friendship WHERE user_id = ? AND friendship_status_id = 2", Long.class, user2.getId());
		assertThat(friendsOfUser2).isEmpty();
	}

	private User makeUser(String email, String login, String name, LocalDate birthday) {
		User user = new User();
		user.setEmail(email);
		user.setLogin(login);
		user.setName(name);
		user.setBirthday(birthday);
		return user;
	}

	private Film makeFilm(String name, int duration, int ratingId) {
		Film film = new Film();
		film.setName(name);
		film.setDescription(name + " description");
		film.setReleaseDate(LocalDate.of(2020, 5, 5));
		film.setDuration(duration);

		Rating rating = new Rating();
		rating.setId(ratingId);
		film.setRating(rating);

		film.setGenres(new HashSet<>());
		return film;
	}
}