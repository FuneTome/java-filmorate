CREATE TABLE IF NOT EXISTS rating (
    rating_id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS friendship_status (
    friendship_status_id INTEGER PRIMARY KEY,
    status VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS film (
    film_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    rating_id INTEGER,
    FOREIGN KEY (rating_id) REFERENCES rating(rating_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR NOT NULL,
    login VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS friendship (
    user_id INTEGER,
    friend_id INTEGER,
    friendship_status_id INTEGER,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (friend_id) REFERENCES users(user_id),
    FOREIGN KEY (friendship_status_id) REFERENCES friendship_status(friendship_status_id)
    );

CREATE TABLE IF NOT EXISTS film_like (
    user_id INTEGER,
    film_id INTEGER,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id)
    );

CREATE TABLE IF NOT EXISTS film_genre (
    genre_id INTEGER,
    film_id INTEGER,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id)
);