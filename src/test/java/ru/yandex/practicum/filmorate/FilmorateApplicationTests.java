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
		assertEquals("Логин не может быть пустым и не должен содержать пробелы", violations.iterator().next().getMessage());
	}

	@Test
	void shouldFailWhenUserLoginContainsSpaces() {
		User user = createValidUser();
		user.setLogin("login with spaces");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
		assertEquals("Логин не может быть пустым и не должен содержать пробелы", violations.iterator().next().getMessage());
	}

	@Test
	void shouldUseLoginAsNameWhenNameIsEmpty() {
		User user = createValidUser();
		user.setLogin("testLogin");
		user.setName("");

		assertEquals("testLogin", user.getName());
		assertEquals("testLogin", user.getLogin());
	}

	@Test
	void shouldUseLoginAsNameWhenNameIsNull() {
		User user = createValidUser();
		user.setLogin("testLogin");
		user.setName(null);

		assertEquals("testLogin", user.getName());
		assertEquals("testLogin", user.getLogin());
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
	void shouldFailWhenUserBirthdayIsNull() {
		User user = createValidUser();
		user.setBirthday(null);

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
	}

	@Test
	void shouldFailWhenUserBirthdayIsInFuture() {
		User user = createValidUser();
		user.setBirthday(LocalDate.of(2030, 12, 31));

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());
	}

	@Test
	void shouldPassWhenUserBirthdayIsToday() {
		User user = createValidUser();
		user.setBirthday(LocalDate.now());

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldPassWhenUserBirthdayIsPast() {
		User user = createValidUser();
		user.setBirthday(LocalDate.of(1990, 5, 15));

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertTrue(violations.isEmpty());
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
	void shouldFailWhenFilmNameIsNull() {
		Film film = createValidFilm();
		film.setName(null);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
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
	void shouldAllowFilmDescriptionIsNull() {
		Film film = createValidFilm();
		film.setDescription(null);

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
	void shouldPassWhenFilmDurationIsPositive() {
		Film film = createValidFilm();
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldFailWhenFilmReleaseDateIsBeforeMinimum() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 27));

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
	}

	@Test
	void shouldPassWhenFilmReleaseDateIsMinimum() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 28));

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldPassWhenFilmReleaseDateIsAfterMinimum() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(2000, 1, 1));

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldFailWhenFilmReleaseDateIsNull() {
		Film film = createValidFilm();
		film.setReleaseDate(null);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertFalse(violations.isEmpty());
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
	void shouldPassWhenUserEmailHasUppercase() {
		User user = createValidUser();
		user.setEmail("User@Example.COM");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldPassWhenUserLoginIsSingleCharacter() {
		User user = createValidUser();
		user.setLogin("a");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldPassWhenFilmNameIsSingleCharacter() {
		Film film = createValidFilm();
		film.setName("A");

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldPassWhenFilmDurationIsOne() {
		Film film = createValidFilm();
		film.setDuration(1);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	@Test
	void shouldHandleVeryLongValidDescription() {
		Film film = createValidFilm();
		film.setDescription("This is a valid description that is exactly 200 characters long. " +
				"Let me write some more text to reach the limit. " +
				"Now it should be exactly 200. Done!");

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertTrue(violations.isEmpty());
	}

	private User createValidUser() {
		User user = new User();
		user.setEmail("user@example.com");
		user.setLogin("validLogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(2010, 7, 16));
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