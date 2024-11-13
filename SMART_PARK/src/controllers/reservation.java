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
import java.net.URL;
import java.sql.*;
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
    private void makeReservation() {
        String vehicleNumber = vehicleNumberField.getText();
        String vehicleType = typeChoiceBox.getValue();
        String selectedSlot = slotChoiceBox.getValue();
        Date startDate = Date.valueOf(startDatePicker.getValue());
        Date endDate = Date.valueOf(endDatePicker.getValue());
        String fromTime = fromTimeField.getText();
        String toTime = toTimeField.getText();

        if (vehicleNumber.isEmpty() || vehicleType == null || selectedSlot == null || startDate == null || endDate == null || fromTime.isEmpty() || toTime.isEmpty()) {
            showAlert("Error", "Please fill in all fields.", Alert.AlertType.ERROR);
            return;
        }

        // Insert reservation into the database
        String query = "INSERT INTO reservations (vehicle_number, vehicle_type, slot_id, start_date, end_date, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, vehicleNumber);
            preparedStatement.setString(2, vehicleType);
            preparedStatement.setString(3, selectedSlot);
            preparedStatement.setDate(4, startDate);
            preparedStatement.setDate(5, endDate);
            preparedStatement.setString(6, fromTime);
            preparedStatement.setString(7, toTime);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Reservation made successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to make reservation.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to make reservation due to database error.", Alert.AlertType.ERROR);
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
}