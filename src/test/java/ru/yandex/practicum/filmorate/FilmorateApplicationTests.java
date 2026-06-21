package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private FilmService filmService;

	private User testUser1;
	private User testUser2;
	private User testUser3;
	private Film testFilm1;
	private Film testFilm2;
	private Film testFilm3;

	@BeforeEach
	void setUp() {
		testUser1 = new User();
		testUser1.setEmail("user1@example.com");
		testUser1.setLogin("user1");
		testUser1.setName("User One");
		testUser1.setBirthday(LocalDate.of(1990, 1, 1));

		testUser2 = new User();
		testUser2.setEmail("user2@example.com");
		testUser2.setLogin("user2");
		testUser2.setName("User Two");
		testUser2.setBirthday(LocalDate.of(1991, 2, 2));

		testUser3 = new User();
		testUser3.setEmail("user3@example.com");
		testUser3.setLogin("user3");
		testUser3.setName("User Three");
		testUser3.setBirthday(LocalDate.of(1992, 3, 3));

		testFilm1 = new Film();
		testFilm1.setName("Film One");
		testFilm1.setDescription("Description One");
		testFilm1.setReleaseDate(LocalDate.of(2020, 1, 1));
		testFilm1.setDuration(120);

		testFilm2 = new Film();
		testFilm2.setName("Film Two");
		testFilm2.setDescription("Description Two");
		testFilm2.setReleaseDate(LocalDate.of(2021, 2, 2));
		testFilm2.setDuration(130);

		testFilm3 = new Film();
		testFilm3.setName("Film Three");
		testFilm3.setDescription("Description Three");
		testFilm3.setReleaseDate(LocalDate.of(2022, 3, 3));
		testFilm3.setDuration(140);
	}

	@Test
	void shouldAddFriend() {
		User createdUser1 = userService.createUser(testUser1);
		User createdUser2 = userService.createUser(testUser2);

		userService.addFriend(createdUser1.getId(), createdUser2.getId());

		List<User> friends = userService.getAllFriends(createdUser1.getId());
		assertEquals(1, friends.size());
		assertEquals(createdUser2.getId(), friends.getFirst().getId());
	}

	@Test
	void shouldDeleteFriend() {
		User createdUser1 = userService.createUser(testUser1);
		User createdUser2 = userService.createUser(testUser2);

		userService.addFriend(createdUser1.getId(), createdUser2.getId());
		userService.deleteFriend(createdUser1.getId(), createdUser2.getId());

		List<User> friends = userService.getAllFriends(createdUser1.getId());
		assertTrue(friends.isEmpty());
	}

	@Test
	void shouldGetAllFriends() {
		User createdUser1 = userService.createUser(testUser1);
		User createdUser2 = userService.createUser(testUser2);
		User createdUser3 = userService.createUser(testUser3);

		userService.addFriend(createdUser1.getId(), createdUser2.getId());
		userService.addFriend(createdUser1.getId(), createdUser3.getId());

		List<User> friends = userService.getAllFriends(createdUser1.getId());
		assertEquals(2, friends.size());
	}

	@Test
	void shouldGetCommonFriends() {
		User createdUser1 = userService.createUser(testUser1);
		User createdUser2 = userService.createUser(testUser2);
		User createdUser3 = userService.createUser(testUser3);

		userService.addFriend(createdUser1.getId(), createdUser3.getId());
		userService.addFriend(createdUser2.getId(), createdUser3.getId());

		List<User> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());
		assertEquals(1, commonFriends.size());
		assertEquals(createdUser3.getId(), commonFriends.getFirst().getId());
	}

	@Test
	void shouldGetEmptyCommonFriends() {
		User createdUser1 = userService.createUser(testUser1);
		User createdUser2 = userService.createUser(testUser2);
		User createdUser3 = userService.createUser(testUser3);

		userService.addFriend(createdUser1.getId(), createdUser3.getId());

		List<User> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());
		assertTrue(commonFriends.isEmpty());
	}

	@Test
	void shouldAddLike() {
		User createdUser = userService.createUser(testUser1);
		Film createdFilm = filmService.createFilm(testFilm1);

		filmService.addLike(createdFilm.getId(), createdUser.getId());

		Film film = filmService.getFilm(createdFilm.getId());
		assertEquals(1, film.getLikes().size());
		assertTrue(film.getLikes().contains(createdUser.getId()));
	}

	@Test
	void shouldRemoveLike() {
		User createdUser = userService.createUser(testUser1);
		Film createdFilm = filmService.createFilm(testFilm1);

		filmService.addLike(createdFilm.getId(), createdUser.getId());
		filmService.removeLike(createdFilm.getId(), createdUser.getId());

		Film film = filmService.getFilm(createdFilm.getId());
		assertTrue(film.getLikes().isEmpty());
	}

	@Test
	void shouldGetPopularFilmsWithCustomCount() {
		User user1 = userService.createUser(testUser1);
		User user2 = userService.createUser(testUser2);

		Film film1 = filmService.createFilm(testFilm1);
		Film film2 = filmService.createFilm(testFilm2);
		Film film3 = filmService.createFilm(testFilm3);

		filmService.addLike(film1.getId(), user1.getId());
		filmService.addLike(film1.getId(), user2.getId());
		filmService.addLike(film2.getId(), user1.getId());
		filmService.addLike(film3.getId(), user1.getId());

		List<Film> popularFilms = filmService.getPopularFilms(2L);
		assertEquals(2, popularFilms.size());
		assertEquals(film1.getId(), popularFilms.get(0).getId());
		assertEquals(film2.getId(), popularFilms.get(1).getId());
	}

	@Test
	void shouldGetPopularFilmsWithCountGreaterThanAvailable() {
		User user1 = userService.createUser(testUser1);

		Film film1 = filmService.createFilm(testFilm1);
		Film film2 = filmService.createFilm(testFilm2);

		filmService.addLike(film1.getId(), user1.getId());

		List<Film> popularFilms = filmService.getPopularFilms(5L);
		assertEquals(2, popularFilms.size());
	}


	@Test
	void shouldAddFriendAndCheckMutualFriendship() {
		User createdUser1 = userService.createUser(testUser1);
		User createdUser2 = userService.createUser(testUser2);

		userService.addFriend(createdUser1.getId(), createdUser2.getId());

		List<User> friendsOfUser1 = userService.getAllFriends(createdUser1.getId());
		List<User> friendsOfUser2 = userService.getAllFriends(createdUser2.getId());

		assertEquals(1, friendsOfUser1.size());
		assertEquals(1, friendsOfUser2.size());
		assertEquals(createdUser2.getId(), friendsOfUser1.getFirst().getId());
		assertEquals(createdUser1.getId(), friendsOfUser2.getFirst().getId());
	}

	@Test
	void shouldThrowExceptionWhenDeletingNonExistentFriend() {
		User createdUser1 = userService.createUser(testUser1);
		User createdUser2 = userService.createUser(testUser2);

		assertThrows(NotFoundException.class, () ->
				userService.deleteFriend(createdUser1.getId(), createdUser2.getId())
		);
	}

	@Test
	void shouldThrowExceptionWhenAddingLikeTwice() {
		User createdUser = userService.createUser(testUser1);
		Film createdFilm = filmService.createFilm(testFilm1);

		filmService.addLike(createdFilm.getId(), createdUser.getId());
		assertThrows(IllegalStateException.class, () ->
				filmService.addLike(createdFilm.getId(), createdUser.getId())
		);
	}

	@Test
	void shouldThrowExceptionWhenRemovingNonExistentLike() {
		User createdUser = userService.createUser(testUser1);
		Film createdFilm = filmService.createFilm(testFilm1);

		assertThrows(NotFoundException.class, () ->
				filmService.removeLike(createdFilm.getId(), createdUser.getId())
		);
	}

	@Test
	void shouldThrowExceptionWhenAddingFriendToNonExistentUser() {
		User createdUser = userService.createUser(testUser1);

		assertThrows(NotFoundException.class, () ->
				userService.addFriend(createdUser.getId(), 999L)
		);
	}

	@Test
	void shouldThrowExceptionWhenGettingFriendsOfNonExistentUser() {
		assertThrows(NotFoundException.class, () ->
				userService.getAllFriends(999L)
		);
	}

	@Test
	void shouldThrowExceptionWhenGettingCommonFriendsWithNonExistentUser() {
		User createdUser = userService.createUser(testUser1);

		assertThrows(NotFoundException.class, () ->
				userService.getCommonFriends(createdUser.getId(), 999L)
		);
	}

	@Test
	void shouldThrowExceptionWhenAddingLikeToNonExistentFilm() {
		User createdUser = userService.createUser(testUser1);

		assertThrows(NotFoundException.class, () ->
				filmService.addLike(999L, createdUser.getId())
		);
	}

	@Test
	void shouldThrowExceptionWhenAddingLikeFromNonExistentUser() {
		Film createdFilm = filmService.createFilm(testFilm1);

		assertThrows(NotFoundException.class, () ->
				filmService.addLike(createdFilm.getId(), 999L)
		);
	}

	@Test
	void shouldHandleMultipleLikesFromDifferentUsers() {
		User user1 = userService.createUser(testUser1);
		User user2 = userService.createUser(testUser2);
		User user3 = userService.createUser(testUser3);
		Film createdFilm = filmService.createFilm(testFilm1);

		filmService.addLike(createdFilm.getId(), user1.getId());
		filmService.addLike(createdFilm.getId(), user2.getId());
		filmService.addLike(createdFilm.getId(), user3.getId());

		Film film = filmService.getFilm(createdFilm.getId());
		assertEquals(3, film.getLikes().size());
		assertTrue(film.getLikes().contains(user1.getId()));
		assertTrue(film.getLikes().contains(user2.getId()));
		assertTrue(film.getLikes().contains(user3.getId()));
	}

	@Test
	void shouldHandleRemoveLikeFromFilmWithMultipleLikes() {
		User user1 = userService.createUser(testUser1);
		User user2 = userService.createUser(testUser2);
		Film createdFilm = filmService.createFilm(testFilm1);

		filmService.addLike(createdFilm.getId(), user1.getId());
		filmService.addLike(createdFilm.getId(), user2.getId());

		filmService.removeLike(createdFilm.getId(), user1.getId());

		Film film = filmService.getFilm(createdFilm.getId());
		assertEquals(1, film.getLikes().size());
		assertTrue(film.getLikes().contains(user2.getId()));
		assertFalse(film.getLikes().contains(user1.getId()));
	}
}