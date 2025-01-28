-- Создаём таблицу владельцев
CREATE TABLE owners
(
    email VARCHAR(100) PRIMARY KEY, -- Email как уникальный идентификатор
    name  VARCHAR(100) NOT NULL
);

-- Создаём таблицу товаров
CREATE TABLE items
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    owner_email VARCHAR(100) NOT NULL,
    CONSTRAINT fk_owner_email FOREIGN KEY (owner_email) REFERENCES owners (email)
);

-- Добавляем 20 владельцев
INSERT INTO owners (email, name)
VALUES ('owner1@example.com', 'Owner 1'),
       ('owner2@example.com', 'Owner 2'),
       ('owner3@example.com', 'Owner 3'),
       ('owner4@example.com', 'Owner 4'),
       ('owner5@example.com', 'Owner 5'),
       ('owner6@example.com', 'Owner 6'),
       ('owner7@example.com', 'Owner 7'),
       ('owner8@example.com', 'Owner 8'),
       ('owner9@example.com', 'Owner 9'),
       ('owner10@example.com', 'Owner 10'),
       ('owner11@example.com', 'Owner 11'),
       ('owner12@example.com', 'Owner 12'),
       ('owner13@example.com', 'Owner 13'),
       ('owner14@example.com', 'Owner 14'),
       ('owner15@example.com', 'Owner 15'),
       ('owner16@example.com', 'Owner 16'),
       ('owner17@example.com', 'Owner 17'),
       ('owner18@example.com', 'Owner 18'),
       ('owner19@example.com', 'Owner 19'),
       ('owner20@example.com', 'Owner 20');

-- Добавляем по 2 товара для каждого владельца
INSERT INTO items (name, owner_email)
SELECT CONCAT('ItemA_of_', o.email), o.email
FROM owners o
UNION ALL
SELECT CONCAT('ItemB_of_', o.email), o.email
FROM owners o;
