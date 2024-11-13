package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class reservation implements Initializable {

    @FXML
    private TextField vehicleNumberField;
    @FXML
    private ChoiceBox<String> typeChoiceBox;
    @FXML
    private ChoiceBox<String> slotChoiceBox;
    @FXML
    private Button makeReservationButton;
    @FXML
    private Button cancelButton;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField fromTimeField;
    @FXML
    private TextField toTimeField;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/smart_park";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate typeChoiceBox with "Bike" and "Car" options
        typeChoiceBox.getItems().addAll("Bike", "Car");
        // Load available slots from the database
        loadAvailableSlots();
    }

    private void loadAvailableSlots() {
        List<String> availableSlots = new ArrayList<>();
        String query = "SELECT slotID FROM slots WHERE availability = 'available'";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Add each available slot ID to the list
            while (resultSet.next()) {
                availableSlots.add(resultSet.getString("slotID"));
            }

            // Populate the slotChoiceBox with available slots
            slotChoiceBox.getItems().clear();
            slotChoiceBox.getItems().addAll(availableSlots);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load available slots from database.", Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void switchTouserHome() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userHome.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go to user home.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void makeReservation() {
        // Step 1: Collect form data
        String vehicleNumber = vehicleNumberField.getText();
        String vehicleType = typeChoiceBox.getValue();
        String selectedSlot = slotChoiceBox.getValue();
        Date startDate = Date.valueOf(startDatePicker.getValue());
        Date endDate = Date.valueOf(endDatePicker.getValue());
        String fromTime = fromTimeField.getText();
        String toTime = toTimeField.getText();

        // Step 2: Validate the form data
        if (vehicleNumber.isEmpty() || vehicleType == null || selectedSlot == null || startDate == null || endDate == null || fromTime.isEmpty() || toTime.isEmpty()) {
            showAlert("Error", "Please fill in all fields.", Alert.AlertType.ERROR);
            return;
        }

        // Step 3: Retrieve user ID from SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        String username = sessionManager.getUsername();
        int userID = getUserIDByUsername(username); // Fetch the userID based on the username.

        // Step 4: Check for time conflicts
        if (checkTimeConflict(startDate, endDate, fromTime, toTime, selectedSlot)) {
            showAlert("Error", "Time conflict: The slot is already reserved for the selected time period.", Alert.AlertType.ERROR);
            return;
        }

        // Step 5: Calculate duration
        int duration = calculateDuration(startDate, endDate, fromTime, toTime);

        // Step 6: Insert reservation into transactions and update slot availability
        String transactionQuery = "INSERT INTO transactions (userID, vehicleID, slotID, cost, startDate, endDate, startTime, endTime, duration, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSlotQuery = "UPDATE slots SET availability = 'reserved', vehicleID = ?, startDate = ?, startTime = ?, endDate = ?, endTime = ? WHERE slotID = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement transactionStatement = connection.prepareStatement(transactionQuery);
             PreparedStatement updateSlotStatement = connection.prepareStatement(updateSlotQuery)) {

            // Set parameters for transactions table insertion
            transactionStatement.setInt(1, userID);
            transactionStatement.setInt(2, getVehicleIDByNumber(vehicleNumber));
            transactionStatement.setInt(3, Integer.parseInt(selectedSlot));
            transactionStatement.setBigDecimal(4, calculateCost(vehicleType, duration));
            transactionStatement.setDate(5, startDate);
            transactionStatement.setDate(6, endDate);
            transactionStatement.setString(7, fromTime);
            transactionStatement.setString(8, toTime);
            transactionStatement.setInt(9, duration);
            transactionStatement.setString(10, "reserved");

            int rowsAffected = transactionStatement.executeUpdate();

            // Update slot availability to "reserved" in the slots table
            updateSlotStatement.setInt(1, getVehicleIDByNumber(vehicleNumber));
            updateSlotStatement.setDate(2, startDate);
            updateSlotStatement.setString(3, fromTime);
            updateSlotStatement.setDate(4, endDate);
            updateSlotStatement.setString(5, toTime);
            updateSlotStatement.setInt(6, Integer.parseInt(selectedSlot));
            updateSlotStatement.executeUpdate();

            if (rowsAffected > 0) {
                switchTouserHome();
                showAlert("Success", "Reservation made successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to make reservation.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to make reservation due to database error.", Alert.AlertType.ERROR);
        }
    }


    // Method to check if any time conflicts exist for the selected slot and time range
    private boolean checkTimeConflict(Date startDate, Date endDate, String fromTime, String toTime, String selectedSlot) {
        String query = "SELECT * FROM transactions WHERE slotID = ? AND type = 'reserved' AND (startDate <= ? AND endDate >= ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(selectedSlot));
            preparedStatement.setDate(2, startDate);
            preparedStatement.setDate(3, endDate);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();  // If any result exists, there is a conflict
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to calculate the duration between start and end times in minutes
    // Method to calculate the duration between start and end times in minutes
    private int calculateDuration(Date startDate, Date endDate, String fromTime, String toTime) {
        fromTime = fromTime.trim();
        toTime = toTime.trim();

        // Ensure the time format is standardized
        fromTime = fromTime.replaceAll("\\s+", ""); // Remove any extra spaces
        toTime = toTime.replaceAll("\\s+", ""); // Remove any extra spaces

        // Convert time strings to `LocalTime` and calculate the duration in minutes.
        // This method should also account for the date difference.
        try {
            // Ensure correct formatting
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma"); // Corrected format without space
            LocalTime startTime = LocalTime.parse(fromTime, timeFormatter);
            LocalTime endTime = LocalTime.parse(toTime, timeFormatter);

            long durationInMinutes = Duration.between(startTime, endTime).toMinutes();

            // If the end time is earlier than start time, assume it is on the next day.
            if (durationInMinutes < 0) {
                durationInMinutes += 1440;  // Add 24 hours worth of minutes (1440)
            }
            return (int) durationInMinutes;

        } catch (DateTimeParseException e) {
            System.err.println("Error parsing time: " + e.getMessage());
            return 0;  // Return 0 or handle the error accordingly
        }
    }



    // Method to calculate cost based on vehicle type and duration (example implementation)
    private BigDecimal calculateCost(String vehicleType, int duration) {
        BigDecimal costPerMinute = vehicleType.equals("Car") ? BigDecimal.valueOf(2.0) : BigDecimal.valueOf(1.5);
        return costPerMinute.multiply(BigDecimal.valueOf(duration));
    }

    // Method to retrieve userID based on username (this is just a placeholder; implement as per your logic)
    private int getUserIDByUsername(String username) {
        // Retrieve the userID from the database or session
        return 1;  // Placeholder, replace with actual logic
    }

    // Method to retrieve vehicleID based on vehicle number (this is just a placeholder; implement as per your logic)
    private int getVehicleIDByNumber(String vehicleNumber) {
        // Retrieve the vehicleID from the database based on the vehicle number
        return 1;  // Placeholder, replace with actual logic
    }

}