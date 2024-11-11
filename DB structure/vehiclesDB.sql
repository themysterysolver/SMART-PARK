CREATE TABLE vehicles (
    vehicleid INT(11) NOT NULL AUTO_INCREMENT,
    userid INT(11),
    vehicle_type ENUM('car', 'bike') NOT NULL,
    registration_number VARCHAR(50) NOT NULL,
    PRIMARY KEY (vehicleid),
    UNIQUE KEY (registration_number),
    FOREIGN KEY (userid) REFERENCES users(userid) ON DELETE SET NULL
);
