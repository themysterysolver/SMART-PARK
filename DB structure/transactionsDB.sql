CREATE TABLE transactions (
    transactionID INT(11) NOT NULL AUTO_INCREMENT,
    userID INT(11),
    vehicleID INT(11),
    slotID INT(11),
    cost DECIMAL(10, 2),
    startDate DATE,
    endDate DATE,
    startTime TIME,
    endTime TIME,
    duration INT(11),
    type ENUM('booked', 'reserved') NOT NULL,
    PRIMARY KEY (transactionID),
    FOREIGN KEY (userID) REFERENCES users(userid) ON DELETE CASCADE,
    FOREIGN KEY (vehicleID) REFERENCES vehicles(vehicleid) ON DELETE CASCADE,
    FOREIGN KEY (slotID) REFERENCES slots(slotID) ON DELETE CASCADE
);
