CREATE TABLE IF NOT EXISTS Rating (
    rating_id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS Genre (
    genre_id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS Friendship_status (
    friendship_status_id INTEGER PRIMARY KEY,
    status VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS Film (
    film_id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    genre_id INTEGER,
    rating_id INTEGER,
    FOREIGN KEY (genre_id) REFERENCES Genre(genre_id),
    FOREIGN KEY (rating_id) REFERENCES Rating(rating_id)
    );

CREATE TABLE IF NOT EXISTS Users (
    user_id INTEGER PRIMARY KEY,
    email VARCHAR NOT NULL,
    login VARCHAR NOT NULL,
    name VARCHAR,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS Friendship (
    friendship_id INTEGER PRIMARY KEY,
    user_id INTEGER,
    friend_id INTEGER,
    friendship_status_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (friend_id) REFERENCES Users(user_id),
    FOREIGN KEY (friendship_status_id) REFERENCES Friendship_status(friendship_status_id)
);