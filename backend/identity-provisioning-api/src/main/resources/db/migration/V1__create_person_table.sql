CREATE TABLE persons (
    id            BIGSERIAL    PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    document      VARCHAR(11)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    date_of_birth DATE         NOT NULL,
    zip_code      VARCHAR(8),
    street        VARCHAR(255),
    neighborhood  VARCHAR(255),
    city          VARCHAR(255),
    state         VARCHAR(2),
    complement    VARCHAR(255),
    login         VARCHAR(7)   NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_persons_document UNIQUE (document),
    CONSTRAINT uk_persons_login    UNIQUE (login)
);
