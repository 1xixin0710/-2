DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(40) NOT NULL,
    role VARCHAR(20) NOT NULL,
    student_no VARCHAR(30) NOT NULL,
    department VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    publish_year INT NOT NULL,
    category VARCHAR(40) NOT NULL,
    language VARCHAR(20) NOT NULL,
    call_number VARCHAR(50) NOT NULL,
    location VARCHAR(60) NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    cover_url VARCHAR(255) NOT NULL,
    summary VARCHAR(1500) NOT NULL,
    tags VARCHAR(160) NOT NULL,
    acquisition_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_books_category (category),
    INDEX idx_books_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_no VARCHAR(32) NOT NULL UNIQUE,
    book_id BIGINT NOT NULL,
    book_title VARCHAR(120) NOT NULL,
    book_cover_url VARCHAR(255) NOT NULL,
    call_number VARCHAR(50) NOT NULL,
    reader_username VARCHAR(40) NOT NULL,
    reader_name VARCHAR(40) NOT NULL,
    borrow_type VARCHAR(30) NOT NULL,
    pickup_location VARCHAR(60) NOT NULL,
    contact VARCHAR(50) NOT NULL,
    note VARCHAR(300),
    scheduled_pickup_at TIMESTAMP NOT NULL,
    borrow_date TIMESTAMP,
    due_date DATE NOT NULL,
    returned_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_loans_reader (reader_username),
    INDEX idx_loans_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
