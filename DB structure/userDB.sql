CREATE TABLE users(
    userid int AUTO_INCREMENT PRIMARY key,
    username varchar(25) not null UNIQUE,
    password varchar(25) not null,
    type enum('admin','user') not null
    );