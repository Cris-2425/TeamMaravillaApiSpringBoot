CREATE TABLE shopping_lists (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    name VARCHAR(120) NOT NULL,
    background VARCHAR(6) COLLATE utf8mb4_0900_bin NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_shopping_lists PRIMARY KEY (id),
    CONSTRAINT fk_shopping_lists_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ck_shopping_lists_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_shopping_lists_background CHECK (background IN (
        'FONDO1', 'FONDO2', 'FONDO3', 'FONDO4'
    )),
    INDEX idx_shopping_lists_user_created (user_id, created_at DESC, id ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;
