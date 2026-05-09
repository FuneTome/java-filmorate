MERGE INTO Rating (rating_id, name) KEY(rating_id) VALUES (1, 'G');
MERGE INTO Rating (rating_id, name) KEY(rating_id) VALUES (2, 'PG');
MERGE INTO Rating (rating_id, name) KEY(rating_id) VALUES (3, 'PG-13');
MERGE INTO Rating (rating_id, name) KEY(rating_id) VALUES (4, 'R');
MERGE INTO Rating (rating_id, name) KEY(rating_id) VALUES (5, 'NC-17');

MERGE INTO Genre (genre_id, name) KEY(genre_id) VALUES (1, 'Комедия');
MERGE INTO Genre (genre_id, name) KEY(genre_id) VALUES (2, 'Драма');
MERGE INTO Genre (genre_id, name) KEY(genre_id) VALUES (3, 'Мультфильм');
MERGE INTO Genre (genre_id, name) KEY(genre_id) VALUES (4, 'Триллер');
MERGE INTO Genre (genre_id, name) KEY(genre_id) VALUES (5, 'Документальный');
MERGE INTO Genre (genre_id, name) KEY(genre_id) VALUES (6, 'Боевик');

MERGE INTO Friendship_status (friendship_status_id, status) KEY(friendship_status_id) VALUES (1, 'PENDING');
MERGE INTO Friendship_status (friendship_status_id, status) KEY(friendship_status_id) VALUES (2, 'CONFIRMED');