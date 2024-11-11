CREATE TABLE slots (
    slotID INT(11) NOT NULL AUTO_INCREMENT,
    availability ENUM('available', 'reserved', 'booked') NOT NULL DEFAULT 'available',
    vehicleID INT(11) DEFAULT NULL,
    PRIMARY KEY (slotID),
    FOREIGN KEY (vehicleID) REFERENCES vehicles(vehicleid) ON DELETE SET NULL
);