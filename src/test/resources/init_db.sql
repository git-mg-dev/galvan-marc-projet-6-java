DROP TABLE IF EXISTS contact;
DROP TABLE IF EXISTS operation;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
        id int NOT NULL AUTO_INCREMENT,
        firstname varchar(255),
        lastname varchar(255),
        email varchar(255) NOT NULL,
        password varchar(255) NOT NULL,
        account_balance float NOT NULL DEFAULT '0.00',
        status varchar(20) DEFAULT 'ENABLED',
        creation_date TIMESTAMP NOT NULL,
        deletion_date TIMESTAMP,
        openidconnect_user BIT NOT NULL DEFAULT 0,
        PRIMARY KEY(id)
);

CREATE TABLE contact (
        id_user_1 int NOT NULL,
        id_user_2 int NOT NULL,
        PRIMARY KEY(id_user_1,id_user_2),
        CONSTRAINT `user1_user2_ibfk_1` FOREIGN KEY (`id_user_1`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT `user2_user1_ibfk_1` FOREIGN KEY (`id_user_2`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE operation (
        operation_id int NOT NULL AUTO_INCREMENT,
        operation_date TIMESTAMP NOT NULL,
        operation_type varchar(20) NOT NULL,
        description varchar(255) NOT NULL,
        amount float NOT NULL,
        charged_amount float NOT NULL DEFAULT 0.0,
        id_sender int NOT NULL,
        id_recipient int NOT NULL,
        iban varchar(50),
        status varchar(20) DEFAULT 'SUCCEEDED',
        PRIMARY KEY(operation_id),
        CONSTRAINT `sender_operation_ibfk_1` FOREIGN KEY (`id_sender`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
        CONSTRAINT `recipient_operation_ibfk_1` FOREIGN KEY (`id_recipient`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO user (firstname, lastname, email, password, account_balance, creation_date) VALUES
('Pauline', 'Test', 'pauline.test@mail.com', '$2y$10$RBGW8BwRgecaDpWX1LNr4Oik1qe37JdAS9lpmGbO0skTnQjBuGzHa', 200, '2023-12-01'),
('Jeanne', 'Rocher', 'jrocher@mail.com', '$2y$10$RBGW8BwRgecaDpWX1LNr4Oik1qe37JdAS9lpmGbO0skTnQjBuGzHa', 0, '2023-12-02');
INSERT INTO user (firstname, lastname, email, password, account_balance, status, creation_date, deletion_date) VALUES
('Marius', 'Richard', 'mr@mail.com', '$2y$10$RBGW8BwRgecaDpWX1LNr4Oik1qe37JdAS9lpmGbO0skTnQjBuGzHa', 0, 'DISABLED', '2023-12-02', '2023-12-03');
INSERT INTO user (firstname, lastname, email, password, account_balance, creation_date, openidconnect_user) VALUES
('OpenId', 'Connect', 'openid.connect@gmail.com', '$2y$10$RBGW8BwRgecaDpWX1LNr4Oik1qe37JdAS9lpmGbO0skTnQjBuGzHa', 45.86, '2023-12-01', 1);
INSERT INTO user (firstname, lastname, email, password, status, account_balance, creation_date, deletion_date, openidconnect_user) VALUES
('OpenId', 'Disabled', 'openid.disabled@gmail.com', '$2y$10$RBGW8BwRgecaDpWX1LNr4Oik1qe37JdAS9lpmGbO0skTnQjBuGzHa', 'DISABLED', 0, '2023-12-01', '2023_12_02', 1);


INSERT INTO contact (id_user_1, id_user_2) VALUES (1,2), (1,3), (2,4);

INSERT INTO operation (operation_date, operation_type, description, amount, charged_amount, id_sender, id_recipient) VALUES
('2023-12-01', 'DEPOSIT', 'Initial deposit', 199.0, 1, 1, 1),
( '2023-12-02', 'PAYMENT', 'Restaurant', 23.4, 0.12, 1, 2),
( '2023-12-03', 'PAYMENT', 'Restaurant2', 23.4, 0.12, 1, 2),
( '2023-12-04', 'PAYMENT', 'Restaurant3', 23.4, 0.12, 1, 2),
( '2023-12-05', 'PAYMENT', 'Leboncoin1', 5, 0.03, 1, 3),
( '2023-12-06', 'PAYMENT', 'Leboncoin2', 5, 0.03, 1, 3),
( '2023-12-07', 'PAYMENT', 'Leboncoin3', 5, 0.03, 1, 3),
( '2023-12-08', 'PAYMENT', 'Leboncoin4', 5, 0.03, 1, 3),
( '2023-12-09', 'PAYMENT', 'Leboncoin5', 5, 0.03, 1, 4),
( '2023-12-10', 'PAYMENT', 'Leboncoin6', 5, 0.03, 1, 4),
( '2023-12-11', 'PAYMENT', 'Ciné', 5, 0.03, 1, 4),
( '2023-12-12', 'PAYMENT', 'Bowling', 5, 0.03, 1, 4),
( '2023-12-13', 'PAYMENT', 'Resto', 5, 0.03, 1, 4),
( '2023-12-14', 'PAYMENT', 'Achats', 5, 0.03, 1, 4),
( '2023-12-15', 'PAYMENT', 'Anniv Julie', 5, 0.03, 1, 4),
( '2023-12-16', 'PAYMENT', 'Vinted', 5, 0.03, 1, 4),
( '2023-12-17', 'PAYMENT', 'Courses', 5, 0.03, 1, 4),
( '2023-12-17', 'PAYMENT', 'Truc', 5, 0.03, 1, 4),
( '2023-12-17', 'PAYMENT', 'Essence', 5, 0.03, 1, 4),
( '2023-12-17', 'PAYMENT', 'Train', 5, 0.03, 1, 4),
( '2023-12-17', 'PAYMENT', 'Courses', 5, 0.03, 1, 2),
( '2023-12-17', 'PAYMENT', 'Chocolats', 5, 0.03, 1, 4),
( '2023-12-17', 'PAYMENT', 'Raclette', 5, 0.03, 1, 4);
INSERT INTO operation (operation_date, operation_type, description, amount, charged_amount, id_sender, id_recipient, iban) VALUES
( '2023-12-02', 'TRANSFER', 'Transfer', 4.97, 0.03, 3, 3, 'FR6114508000403618798166B64');

UPDATE user SET account_balance=170.45 WHERE id=1;
/*UPDATE user SET account_balance=23.4 WHERE id=2;*/