UPDATE recipes
SET instructions = REPLACE(
    instructions,
    CONCAT(CHAR(13), CHAR(10)),
    CHAR(10)
);