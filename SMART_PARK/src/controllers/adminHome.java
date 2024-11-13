package controllers;

import com.sun.javafx.charts.Legend;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class adminHome {

    public TableView statusTableView;
    @FXML
    private AnchorPane LHS;  // AnchorPane for the left side

    @FXML
    private Button logoutbutton;

    private TilePane slotTilePane;

    @FXML
    private void initialize() {
        // Initialize the TilePane and add it to LHS AnchorPane
        slotTilePane = new TilePane();
        slotTilePane.setHgap(10);  // Horizontal gap between buttons
        slotTilePane.setVgap(10);  // Vertical gap between buttons
        slotTilePane.setPrefColumns(3);  // Number of columns in the TilePane
        slotTilePane.setPrefWidth(LHS.getPrefWidth());  // Set width to fit LHS
        slotTilePane.setPrefHeight(LHS.getPrefHeight());  // Set height to fit LHS

        LHS.getChildren().add(slotTilePane);
        AnchorPane.setTopAnchor(slotTilePane, 0.0);
        AnchorPane.setBottomAnchor(slotTilePane, 0.0);
        AnchorPane.setLeftAnchor(slotTilePane, 0.0);
        AnchorPane.setRightAnchor(slotTilePane, 0.0);

        fetchSlotData(); // Fetch and display slots on initialization

        TableColumn<SlotStatus, String> statusColumn = new TableColumn<>("STATUS");
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        TableColumn<SlotStatus, Integer> countColumn = new TableColumn<>("COUNT");
        countColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCount()).asObject());

        statusTableView.getColumns().add(statusColumn);
        statusTableView.getColumns().add(countColumn);

        fetchSlotCounts();  // Fetch and display slot counts on initialization
    }
    private void fetchSlotCounts() {
        String url = "jdbc:mysql://localhost:3306/smart_park";  // Your DB URL
        String user = "root";  // Your DB username
        String password = "";  // Your DB password

        String query = "SELECT availability, COUNT(*) AS count FROM slots GROUP BY availability";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Clear existing data
            ObservableList<SlotStatus> statusList = FXCollections.observableArrayList();

            // Iterate over result set and create SlotStatus objects
            while (rs.next()) {
                String availability = rs.getString("availability");
                int count = rs.getInt("count");

                // Add status and count to the list
                statusList.add(new SlotStatus(availability, count));
            }

            // Set data to the TableView
            statusTableView.setItems(statusList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch slot counts from the database.", Alert.AlertType.ERROR);
        }
    }


    private void fetchSlotData() {
        // Database connection setup (use your actual database credentials here)
        String url = "jdbc:mysql://localhost:3306/smart_park";  // Change to your DB URL
        String user = "root";  // Your DB username
        String password = "";  // Your DB password

        String query = "SELECT slotID, availability, vehicleID, startDate, startTime FROM slots";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Iterate over result set and create a button for each slot
            while (rs.next()) {
                int slotID = rs.getInt("slotID");
                String availability = rs.getString("availability");
                Integer vehicleID = rs.getObject("vehicleID", Integer.class);  // If vehicleID is NULL, return null

                String startDate = rs.getString("startDate");
                String startTime = rs.getString("startTime");


                String vehicleNumber = null;
                if (vehicleID != null) {
                    vehicleNumber = getVehicleNumber(vehicleID);  // Method to fetch vehicle number from vehicle table
                }

                // Create a button for the slot
                Button slotButton = new Button();
                slotButton.setPrefSize(80, 80);  // Set preferred size for each slot button

                // Set button text and style based on availability
                if ("available".equals(availability)) {
                    slotButton.setText("Slot " + slotID + "\nAvailable");
                    slotButton.setStyle("-fx-background-color: #90EE90;"); // Green for available
                } else if ("reserved".equals(availability)) {
                    slotButton.setText("Slot " + slotID + "\nReserved\n" + (vehicleNumber != null ? vehicleNumber : ""));
                    slotButton.setStyle("-fx-background-color: #FFD700;"); // Yellow for reserved
                } else if ("booked".equals(availability)) {
                    slotButton.setText("Slot " + slotID + "\nBooked\n" + (vehicleNumber != null ? vehicleNumber : ""));
                    slotButton.setStyle("-fx-background-color: #FF6347;"); // Red for booked
                    slotButton.setOnAction(event -> handleBookedSlot(slotID, vehicleID, startDate, startTime));
                }

                // Add the button to the TilePane
                slotTilePane.getChildren().add(slotButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch slot data from the database.", Alert.AlertType.ERROR);
        }
    }
    // Assuming each slot has a booking with a userID. You fetch it when the booking is being completed.



    private void handleBookedSlot(int slotID, Integer vehicleID, String startDate, String startTime) {
        // Show a dialog to complete booking
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Complete Booking");
        alert.setHeaderText("Do you want to complete the booking for Slot " + slotID + "?");

        // Wait for the user's response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                completeBooking(slotID, vehicleID, startDate, startTime);
            }
        });
    }
    private void completeBooking(int slotID, Integer vehicleID, String startDate, String startTime) {
        // Calculate the duration and cost
        long durationInMinutes = calculateDuration(startDate, startTime);
        double costPerHour = getCostPerHour(vehicleID);
        double totalCost = (costPerHour / 60) * durationInMinutes;

        // Get current time for endDate and endTime
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String endDate = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Fetch the userID associated with the vehicle
        Integer userID = getUserIDForVehicle(vehicleID);

        // Insert the transaction record into the transaction table
        String transactionQuery = "INSERT INTO transactions (slotID, vehicleID, userID, cost, startDate, startTime, endDate, endTime, duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "");
             PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
            stmt.setInt(1, slotID);
            stmt.setInt(2, vehicleID);
            stmt.setInt(3, userID);  // Insert the userID into the transaction
            stmt.setDouble(4, totalCost);
            stmt.setString(5, startDate);
            stmt.setString(6, startTime);
            stmt.setString(7, endDate);
            stmt.setString(8, endTime);
            stmt.setLong(9, durationInMinutes);  // Insert durationInMinutes
            stmt.executeUpdate();

            // Update the slot status and clear vehicle-related data in the slot table
            String updateSlotQuery = "UPDATE slots SET availability = 'available', vehicleID = NULL, startDate = NULL, startTime = NULL WHERE slotID = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSlotQuery)) {
                updateStmt.setInt(1, slotID);
                updateStmt.executeUpdate();
            }

            // Clear the existing slot buttons and reload the updated data
            slotTilePane.getChildren().clear(); // Clear the old slot buttons
            fetchSlotData();

            // Show success message
            showAlert("Success", "Booking completed successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to complete the booking.", Alert.AlertType.ERROR);
        }
    }

    private Integer getUserIDForVehicle(int vehicleID) {
        String query = "SELECT userid FROM vehicles WHERE vehicleID = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vehicleID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("userid");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;  // Return null if no userID is found
    }


    private long calculateDuration(String startDate, String startTime) {
        // Get the current time
        LocalDateTime currentTime = LocalDateTime.now();

        // Convert startDate and startTime to LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startDateTime = startDate + " " + startTime;
        LocalDateTime startDateTimeParsed = LocalDateTime.parse(startDateTime, formatter);

        // Calculate the duration in minutes
        Duration duration = Duration.between(startDateTimeParsed, currentTime);
        return duration.toMinutes();  // Return the duration in minutes
    }


    private double getCostPerHour(int vehicleID) {
        String query = "SELECT cost FROM cost WHERE type = (SELECT vehicle_type FROM vehicles WHERE vehicleID = ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vehicleID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("cost");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }



    // Method to fetch vehicle number for a given vehicleID
    private String getVehicleNumber(int vehicleID) {
        String vehicleNumber = null;
        String query = "SELECT registration_number FROM vehicles WHERE vehicleID = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vehicleID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    vehicleNumber = rs.getString("registration_number");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch vehicle number.", Alert.AlertType.ERROR);
        }

        return vehicleNumber;
    }


    @FXML
    private void switchToLogin(){
        SessionManager.getInstance().clearSession();
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/signin.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage=(Stage) logoutbutton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to LOGOUT from user home.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void switchToSettings(){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage=(Stage) logoutbutton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go to SEETINGS.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void switchToBooking(){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage=(Stage) logoutbutton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go to Booking.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert=new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
