package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class adminHome {

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
    }

    private void fetchSlotData() {
        // Database connection setup (use your actual database credentials here)
        String url = "jdbc:mysql://localhost:3306/smart_park";  // Change to your DB URL
        String user = "root";  // Your DB username
        String password = "";  // Your DB password

        String query = "SELECT slotID, availability, vehicleID FROM slots";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Iterate over result set and create a button for each slot
            while (rs.next()) {
                int slotID = rs.getInt("slotID");
                String availability = rs.getString("availability");
                Integer vehicleID = rs.getObject("vehicleID", Integer.class);  // If vehicleID is NULL, return null

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
                }

                // Add the button to the TilePane
                slotTilePane.getChildren().add(slotButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch slot data from the database.", Alert.AlertType.ERROR);
        }
    }

    // Method to fetch vehicle number for a given vehicleID
    private String getVehicleNumber(int vehicleID) {
        String vehicleNumber = null;
        String query = "SELECT vehicleNumber FROM vehicles WHERE vehicleID = ?";

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
