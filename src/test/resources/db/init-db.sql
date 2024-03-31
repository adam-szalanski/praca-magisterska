CREATE SEQUENCE IF NOT EXISTS product_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE products
(
    id            BIGINT NOT NULL DEFAULT nextval('product_sequence'),
    name          VARCHAR(255),
    category      VARCHAR(255),
    creation_date date,
    author        VARCHAR(255),
    release_date  date,
    publisher     VARCHAR(255),
    review_score  DOUBLE PRECISION,
    CONSTRAINT pk_products PRIMARY KEY (id)
);