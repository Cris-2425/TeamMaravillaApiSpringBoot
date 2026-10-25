CREATE TABLE recipes (
    id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    image_url VARCHAR(2048) NULL,
    instructions TEXT NOT NULL,
    CONSTRAINT pk_recipes PRIMARY KEY (id),
    CONSTRAINT ck_recipes_id CHECK (id > 0),
    CONSTRAINT ck_recipes_title CHECK (CHAR_LENGTH(TRIM(title)) > 0),
    CONSTRAINT ck_recipes_instructions CHECK (CHAR_LENGTH(TRIM(instructions)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE recipe_products (
    recipe_id INT NOT NULL,
    product_id VARCHAR(64) COLLATE utf8mb4_0900_bin NOT NULL,
    position INT NOT NULL,
    CONSTRAINT pk_recipe_products PRIMARY KEY (recipe_id, product_id),
    CONSTRAINT uk_recipe_products_position UNIQUE (recipe_id, position),
    CONSTRAINT ck_recipe_products_position CHECK (position >= 0),
    CONSTRAINT fk_recipe_products_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id),
    CONSTRAINT fk_recipe_products_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
