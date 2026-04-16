CREATE TABLE IF NOT EXISTS cities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    state VARCHAR(100) DEFAULT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cities_name_state (name, state)
);

CREATE TABLE IF NOT EXISTS theatres (
    id BIGINT NOT NULL AUTO_INCREMENT,
    city_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) DEFAULT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_theatres_city_id (city_id),
    UNIQUE KEY uq_theatres_city_name (city_id, name),
    CONSTRAINT fk_theatres_city FOREIGN KEY (city_id) REFERENCES cities (id)
);

CREATE TABLE IF NOT EXISTS screens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    theatre_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    total_seats INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_screens_theatre_id (theatre_id),
    UNIQUE KEY uq_screens_theatre_name (theatre_id, name),
    CONSTRAINT fk_screens_theatre FOREIGN KEY (theatre_id) REFERENCES theatres (id)
);

CREATE TABLE IF NOT EXISTS screen_seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    screen_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    seat_type VARCHAR(30) NOT NULL DEFAULT 'REGULAR',
    row_label VARCHAR(10) DEFAULT NULL,
    seat_order INT DEFAULT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY idx_screen_seats_screen_id (screen_id),
    UNIQUE KEY uq_screen_seats_screen_number (screen_id, seat_number),
    CONSTRAINT fk_screen_seats_screen FOREIGN KEY (screen_id) REFERENCES screens (id)
);

CREATE TABLE IF NOT EXISTS movie_shows (
    id BIGINT NOT NULL AUTO_INCREMENT,
    movie_id BIGINT NOT NULL,
    screen_id BIGINT NOT NULL,
    show_start DATETIME NOT NULL,
    show_end DATETIME NOT NULL,
    language VARCHAR(50) NOT NULL,
    format VARCHAR(30) NOT NULL DEFAULT '2D',
    base_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_movie_shows_movie_id (movie_id),
    KEY idx_movie_shows_screen_id (screen_id),
    KEY idx_movie_shows_start (show_start),
    UNIQUE KEY uq_movie_shows_screen_start (screen_id, show_start),
    CONSTRAINT fk_movie_shows_movie FOREIGN KEY (movie_id) REFERENCES movie (id),
    CONSTRAINT fk_movie_shows_screen FOREIGN KEY (screen_id) REFERENCES screens (id)
);

CREATE TABLE IF NOT EXISTS show_seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    show_id BIGINT NOT NULL,
    screen_seat_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    locked_until DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_show_seats_show_id (show_id),
    KEY idx_show_seats_screen_seat_id (screen_seat_id),
    UNIQUE KEY uq_show_seats_show_screen_seat (show_id, screen_seat_id),
    CONSTRAINT fk_show_seats_show FOREIGN KEY (show_id) REFERENCES movie_shows (id),
    CONSTRAINT fk_show_seats_screen_seat FOREIGN KEY (screen_seat_id) REFERENCES screen_seats (id)
);

CREATE TABLE IF NOT EXISTS show_bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    booking_code VARCHAR(40) NOT NULL,
    booking_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    PRIMARY KEY (id),
    KEY idx_show_bookings_user_id (user_id),
    KEY idx_show_bookings_show_id (show_id),
    UNIQUE KEY uq_show_bookings_code (booking_code),
    CONSTRAINT fk_show_bookings_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_show_bookings_show FOREIGN KEY (show_id) REFERENCES movie_shows (id)
);

CREATE TABLE IF NOT EXISTS show_booking_seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    show_seat_id BIGINT NOT NULL,
    seat_price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_show_booking_seats_booking_id (booking_id),
    KEY idx_show_booking_seats_show_seat_id (show_seat_id),
    UNIQUE KEY uq_show_booking_seats_show_seat (show_seat_id),
    UNIQUE KEY uq_show_booking_seats_booking_pair (booking_id, show_seat_id),
    CONSTRAINT fk_show_booking_seats_booking FOREIGN KEY (booking_id) REFERENCES show_bookings (id),
    CONSTRAINT fk_show_booking_seats_show_seat FOREIGN KEY (show_seat_id) REFERENCES show_seats (id)
);
