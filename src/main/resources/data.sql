DELETE FROM review_reactions;
DELETE FROM reviews;

DELETE FROM director_film;
DELETE FROM film_genre;
DELETE FROM film_like;

DELETE FROM friendship;

DELETE FROM events;

DELETE FROM users;
DELETE FROM film;
DELETE FROM director;

ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE film ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE director ALTER COLUMN director_id RESTART WITH 1;
ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1;
ALTER TABLE events ALTER COLUMN event_id RESTART WITH 1;

MERGE INTO rating (rating_id, name) KEY(rating_id) VALUES (1, 'G');
MERGE INTO rating (rating_id, name) KEY(rating_id) VALUES (2, 'PG');
MERGE INTO rating (rating_id, name) KEY(rating_id) VALUES (3, 'PG-13');
MERGE INTO rating (rating_id, name) KEY(rating_id) VALUES (4, 'R');
MERGE INTO rating (rating_id, name) KEY(rating_id) VALUES (5, 'NC-17');

MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (1, 'Комедия');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (2, 'Драма');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (3, 'Мультфильм');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (4, 'Триллер');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (5, 'Документальный');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (6, 'Боевик');

MERGE INTO friendship_status (friendship_status_id, status) KEY(friendship_status_id) VALUES (1, 'PENDING');
MERGE INTO friendship_status (friendship_status_id, status) KEY(friendship_status_id) VALUES (2, 'CONFIRMED');
