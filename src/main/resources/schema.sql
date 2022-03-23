CREATE TABLE user
(
username varchar(50) NOT NULL,
name varchar(50) NOT NULL,
password varchar(50) NOT NULL,
PRIMARY KEY (username)
);
CREATE TABLE customer
(
username varchar(50) NOT NULL,
access_expiration Date,
FOREIGN KEY (username) REFERENCES user(username)
);
CREATE TABLE admin
(
username varchar(50) NOT NULL,
FOREIGN KEY (username) REFERENCES user(username)
);