package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class booking implements Initializable {

    @FXML
    private ChoiceBox<String> typeChoiceBox;

    @FXML
    private ChoiceBox<String> slotChoiceBox;

    @FXML
    private TextField vehicleNumberField;

    @FXML
    private TextField userIdField;

    @FXML
    private Button bookButton;

    @FXML
    private Button cancel;

    @FXML
    private TextArea feedbackArea;

    // Database connection details
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
    private void switchToadminHome(){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/adminHome.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage=(Stage) cancel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go to SEETINGS.", Alert.AlertType.ERROR);
        }
    }
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert=new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void generateUsername() {
        String generatedUsername = "";
        int nextId = 1;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "")) {
            // Retrieve the next available ID in users table
            String sql = "SELECT COALESCE(MAX(userid), 0) + 1 AS next_id FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                nextId = rs.getInt("next_id");
            }

            // Generate username based on next available ID
            generatedUsername = "user_" + nextId;

            // Insert new user with generated username
            String insertUser = "INSERT INTO users (username, password, type) VALUES (?, 'xxxx', 'user')";
            PreparedStatement pstmt = conn.prepareStatement(insertUser);
            pstmt.setString(1, generatedUsername);
            pstmt.executeUpdate();

            // Display generated username in userIdField
            userIdField.setText(generatedUsername);

        } catch (Exception e) {
            e.printStackTrace(); // Print the error to console for debugging
        }
    }
    @FXML
    public void bookSlot(ActionEvent actionEvent) {
        String registrationNumber = vehicleNumberField.getText();
        String vehicleType = typeChoiceBox.getValue();
        String selectedSlot = slotChoiceBox.getValue();
        String username = userIdField.getText();

        if (registrationNumber.isEmpty() || vehicleType == null || selectedSlot == null || username.isEmpty()) {
            showAlert("Booking Error", "Please fill in all fields", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            // Insert vehicle record
            String insertVehicleQuery = "INSERT INTO vehicles (userid, vehicle_type, registration_number) VALUES ((SELECT userid FROM users WHERE username = ?), ?, ?)";
            try (PreparedStatement vehicleStmt = conn.prepareStatement(insertVehicleQuery, Statement.RETURN_GENERATED_KEYS)) {
                vehicleStmt.setString(1, username);
                vehicleStmt.setString(2, vehicleType);
                vehicleStmt.setString(3, registrationNumber);
                vehicleStmt.executeUpdate();

                ResultSet generatedKeys = vehicleStmt.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new SQLException("Failed to retrieve vehicle ID.");
                }
                int vehicleID = generatedKeys.getInt(1);

                // Update slot status
                String updateSlotQuery = "UPDATE slots SET availability = 'booked', vehicleID = ?, startDate = ?, startTime = ? WHERE slotID = ?";
                try (PreparedStatement slotStmt = conn.prepareStatement(updateSlotQuery)) {
                    LocalDateTime now = LocalDateTime.now();
                    String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    slotStmt.setInt(1, vehicleID);
                    slotStmt.setString(2, currentDate);
                    slotStmt.setString(3, currentTime);
                    slotStmt.setInt(4, Integer.parseInt(selectedSlot));
                    slotStmt.executeUpdate();
                }
                conn.commit();
            }

            showAlert("Booking Successful", "Booking successful for vehicle " + registrationNumber, Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Booking Error", "Error during booking: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
