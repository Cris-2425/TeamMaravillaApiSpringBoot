CREATE TABLE products (
    id VARCHAR(64) COLLATE utf8mb4_0900_bin NOT NULL,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(20) COLLATE utf8mb4_0900_bin NOT NULL,
    image_url VARCHAR(2048) NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT ck_products_id CHECK (CHAR_LENGTH(TRIM(id)) > 0),
    CONSTRAINT ck_products_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_products_category CHECK (category IN (
        'FRUITS', 'VEGETABLES', 'DAIRY', 'BAKERY', 'MEAT', 'FISH',
        'DRINKS', 'PASTA', 'RICE', 'CLEANING', 'OTHER'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
