package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, DirectorDbStorage.class})
class FilmoRateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final DirectorDbStorage directorStorage;

    @Test
    public void testFindUserById() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(user);

        Optional<User> userOptional = userStorage.findUserById(user.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }

    @Test
    public void testCreateAndUpdateUser() {
        User user = new User();
        user.setEmail("create@test.com");
        user.setLogin("create");
        user.setName("Create User");
        user.setBirthday(LocalDate.of(1995, 5, 5));
        User created = userStorage.create(user);
        assertThat(created.getId()).isNotNull();

        created.setName("Updated Name");
        User updated = userStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testAddAndDeleteFriend() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        user2 = userStorage.create(user2);

        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getAllFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());

        userStorage.deleteFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getAllFriends(user1.getId())).isEmpty();
    }

    @Test
    public void testGetCommonFriends() {
        User user1 = userStorage.create(createUser("u1@t.com", "u1"));
        User user2 = userStorage.create(createUser("u2@t.com", "u2"));
        User commonFriend = userStorage.create(createUser("common@t.com", "common"));

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> common = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(common).hasSize(1);
        assertThat(common.get(0).getId()).isEqualTo(commonFriend.getId());
    }

    @Test
    public void testCreateAndGetFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, "Комедия"));
        film.setGenres(genres);

        Film created = filmStorage.create(film);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getMpa().getId()).isEqualTo(1);
        assertThat(created.getGenres()).hasSize(1);

        Film retrieved = filmStorage.getFilm(created.getId());
        assertThat(retrieved.getName()).isEqualTo("Test Film");
    }

    @Test
    public void testCreateFilmWithInvalidMpaThrowsNotFoundException() {
        Film film = new Film();
        film.setName("Test Film Invalid MPA");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(999, "Unknown"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> filmStorage.create(film))
                .isInstanceOf(ru.yandex.practicum.filmorate.exception.NotFoundException.class);
    }

    @Test
    public void testCreateFilmWithInvalidGenreThrowsNotFoundException() {
        Film film = new Film();
        film.setName("Test Film Invalid Genre");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(999, "Unknown"));
        film.setGenres(genres);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> filmStorage.create(film))
                .isInstanceOf(ru.yandex.practicum.filmorate.exception.NotFoundException.class);
    }

    @Test
    public void testUpdateFilm() {
        Film film = filmStorage.create(createFilm("Film"));
        film.setName("Updated Film Name");
        Film updated = filmStorage.update(film);
        assertThat(updated.getName()).isEqualTo("Updated Film Name");
    }

    @Test
    public void testAddAndRemoveLike() {
        Film film = filmStorage.create(createFilm("Like Film"));
        User user = userStorage.create(createUser("like@t.com", "like"));

        filmStorage.addLike(film, user);
        Film updatedFilm = filmStorage.getFilm(film.getId());
        assertThat(updatedFilm.getLikes()).contains(user.getId());

        filmStorage.removeLike(film, user);
        updatedFilm = filmStorage.getFilm(film.getId());
        assertThat(updatedFilm.getLikes()).doesNotContain(user.getId());
    }

    @Test
    public void testGetPopularFilms() {
        Film film1 = filmStorage.create(createFilm("Film 1"));
        Film film2 = filmStorage.create(createFilm("Film 2"));
        User user = userStorage.create(createUser("u@t.com", "u"));

        filmStorage.addLike(film2, user);

        List<Film> popular = filmStorage.getPopularFilms(2L, null, null);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film2.getId());
    }

    @Test
    public void testGetCommonFilms() {
        User user1 = userStorage.create(createUser("u11@t.com", "u11"));
        User user2 = userStorage.create(createUser("u22@t.com", "u22"));

        Film film1 = filmStorage.create(createFilm("Film A"));
        Film film2 = filmStorage.create(createFilm("Film B"));
        Film film3 = filmStorage.create(createFilm("Film C"));

        filmStorage.addLike(film1, user1);
        filmStorage.addLike(film1, user2);

        filmStorage.addLike(film2, user1);
        filmStorage.addLike(film2, user2);

        filmStorage.addLike(film3, user1);

        User user3 = userStorage.create(createUser("u33@t.com", "u33"));
        filmStorage.addLike(film2, user3);

        List<Film> common = filmStorage.getCommonFilms(user1.getId(), user2.getId());

        assertThat(common).hasSize(2);
        assertThat(common.get(0).getId()).isEqualTo(film2.getId());
        assertThat(common.get(1).getId()).isEqualTo(film1.getId());
    }

    @Test
    public void testSearchByTitleIsCaseInsensitiveAndPartial() {
        filmStorage.create(createFilm("Крадущийся тигр, затаившийся дракон"));
        filmStorage.create(createFilm("Крадущийся в ночи"));
        filmStorage.create(createFilm("Полёт над гнездом кукушки"));

        List<Film> found = filmStorage.searchFilms("КРАД", true, false);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Film::getName)
                .allMatch(name -> name.toLowerCase().contains("крад"));
    }

    @Test
    public void testSearchByDirector() {
        Director tarantino = directorStorage.addDirector(new Director(0, "Квентин Тарантино"));
        Director nolan = directorStorage.addDirector(new Director(0, "Кристофер Нолан"));

        Film pulpFiction = createFilm("Криминальное чтиво");
        pulpFiction.setDirectors(new LinkedHashSet<>(List.of(tarantino)));
        pulpFiction = filmStorage.create(pulpFiction);

        Film inception = createFilm("Начало");
        inception.setDirectors(new LinkedHashSet<>(List.of(nolan)));
        filmStorage.create(inception);

        List<Film> found = filmStorage.searchFilms("тарантино", false, true);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(pulpFiction.getId());
    }

    @Test
    public void testSearchByTitleAndDirectorSortedByPopularity() {
        Director director = directorStorage.addDirector(new Director(0, "Гай Ричи"));

        Film byTitle = filmStorage.create(createFilm("Ричи Рич"));

        Film byDirector = createFilm("Карты, деньги, два ствола");
        byDirector.setDirectors(new LinkedHashSet<>(List.of(director)));
        byDirector = filmStorage.create(byDirector);

        filmStorage.create(createFilm("Посторонний фильм"));

        User user1 = userStorage.create(createUser("s1@t.com", "s1"));
        User user2 = userStorage.create(createUser("s2@t.com", "s2"));
        filmStorage.addLike(byDirector, user1);
        filmStorage.addLike(byDirector, user2);
        filmStorage.addLike(byTitle, user1);

        List<Film> found = filmStorage.searchFilms("ричи", true, true);

        assertThat(found).hasSize(2);
        assertThat(found.get(0).getId()).isEqualTo(byDirector.getId());
        assertThat(found.get(1).getId()).isEqualTo(byTitle.getId());
    }

    @Test
    public void testSearchWithoutCriteriaReturnsEmptyList() {
        filmStorage.create(createFilm("Крадущийся в ночи"));

        assertThat(filmStorage.searchFilms("крад", false, false)).isEmpty();
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2010, 10, 10));
        film.setDuration(100);
        film.setMpa(new Mpa(1, "G"));
        return film;
    }
}
