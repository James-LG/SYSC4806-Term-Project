CREATE TABLE users
(
username varchar(50) NOT NULL,
name varchar(50) NOT NULL,
password varchar(50) NOT NULL,
subscription varchar(50) NOT NULL,
access_expiration Date,
dtype varchar(50),
PRIMARY KEY (username)
);
CREATE TABLE customer
(
username varchar(50) NOT NULL,
FOREIGN KEY (username) REFERENCES users(username)
);
CREATE TABLE admin
(
username varchar(50) NOT NULL,
FOREIGN KEY (username) REFERENCES users(username)
);