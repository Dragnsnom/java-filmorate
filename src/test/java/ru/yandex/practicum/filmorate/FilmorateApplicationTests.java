package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmorateApplicationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void shouldCreateValidUser() {
		User user = createValidUser();
		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldFailWhenUserEmailIsBlank() {
		User user = createValidUser();
		user.setEmail("");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
		assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
	}

	@Test
	void shouldFailWhenUserEmailIsInvalid() {
		User user = createValidUser();
		user.setEmail("invalid-email");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
		assertEquals("Email должен быть корректным", violations.iterator().next().getMessage());
	}

	@Test
	void shouldFailWhenUserLoginIsBlank() {
		User user = createValidUser();
		user.setLogin("");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
		assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
	}

	@Test
	void shouldFailWhenUserLoginContainsSpaces() {
		User user = createValidUser();
		user.setLogin("login with spaces");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
		assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
	}

	@Test
	void shouldUseLoginAsNameWhenNameIsEmpty() {
		User user = createValidUser();
		user.setName("");

		assertTrue(user.getName().isEmpty());
		assertEquals("validLogin", user.getLogin());
	}

	@Test
	void shouldUseLoginAsNameWhenNameIsNull() {
		User user = createValidUser();
		user.setName(null);

		assertNull(user.getName());
		assertEquals("validLogin", user.getLogin());
	}

	@Test
	void shouldFailWithMultipleUserErrors() {
		User user = createValidUser();
		user.setEmail("invalid");
		user.setLogin("login with spaces");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
		assertEquals(2, violations.size());
	}

	@Test
	void shouldCreateValidFilm() {
		Film film = createValidFilm();
		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldFailWhenFilmNameIsBlank() {
		Film film = createValidFilm();
		film.setName("");

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
		assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
	}

	@Test
	void shouldFailWhenFilmDescriptionIsTooLong() {
		Film film = createValidFilm();
		film.setDescription("a".repeat(201));

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
		assertEquals("Описание не длиннее 200 символов", violations.iterator().next().getMessage());
	}

	@Test
	void shouldAllowFilmDescriptionExactlyTwoHundredChars() {
		Film film = createValidFilm();
		film.setDescription("a".repeat(200));

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldAllowFilmDescriptionIsEmpty() {
		Film film = createValidFilm();
		film.setDescription("");

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldFailWhenFilmDurationIsNegative() {
		Film film = createValidFilm();
		film.setDuration(-10);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
		assertEquals("Продолжительность должна быть положительной", violations.iterator().next().getMessage());
	}

	@Test
	void shouldFailWhenFilmDurationIsZero() {
		Film film = createValidFilm();
		film.setDuration(0);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
		assertEquals("Продолжительность должна быть положительной", violations.iterator().next().getMessage());
	}

	@Test
	void shouldFailWhenFilmReleaseDateIsBeforeMinimum() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1890, 1, 1));
		LocalDate minDate = LocalDate.of(1895, 12, 28);

		assertTrue(film.getReleaseDate().isBefore(minDate));
	}

	@Test
	void shouldFailWhenFilmReleaseDateIsBefore28Dec1895() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		LocalDate minDate = LocalDate.of(1895, 12, 28);

		assertTrue(film.getReleaseDate().isBefore(minDate));
	}

	@Test
	void shouldAllowFilmReleaseDateExactlyOnMinimum() {
		Film film = createValidFilm();
		LocalDate minDate = LocalDate.of(1895, 12, 28);
		film.setReleaseDate(minDate);

		assertFalse(film.getReleaseDate().isBefore(minDate));
	}

	@Test
	void shouldPassWhenFilmReleaseDateIs28Dec1895() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		LocalDate minDate = LocalDate.of(1895, 12, 28);

		assertFalse(film.getReleaseDate().isBefore(minDate));
	}

	@Test
	void shouldPassWhenFilmReleaseDateIsAfter28Dec1895() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(2020, 1, 1));
		LocalDate minDate = LocalDate.of(1895, 12, 28);

		assertFalse(film.getReleaseDate().isBefore(minDate));
	}

	@Test
	void shouldFailWithMultipleFilmErrors() {
		Film film = createValidFilm();
		film.setName("");
		film.setDescription("a".repeat(201));
		film.setDuration(-10);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
		assertEquals(3, violations.size());
	}


	@Test
	void shouldFailWhenFilmReleaseDateIsNull() {
		Film film = createValidFilm();
		film.setReleaseDate(null);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
	}

	@Test
	void shouldFailWhenUserBirthdayIsNull() {
		User user = createValidUser();
		user.setBirthday(null);

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
	}

	private User createValidUser() {
		User user = new User();
		user.setEmail("user@example.com");
		user.setLogin("validLogin");
		user.setName("Valid Name");
		user.setBirthday("1990-01-01");
		return user;
	}

	private Film createValidFilm() {
		Film film = new Film();
		film.setName("Inception");
		film.setDescription("A mind-bending thriller");
		film.setReleaseDate(LocalDate.of(2010, 7, 16));
		film.setDuration(148);
		return film;
	}
}