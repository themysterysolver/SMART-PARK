package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class settings {
    @FXML
    private ChoiceBox<Integer> slotChoiceBox;

    @FXML
    private Slider slotSlider;

    @FXML
    private Label sliderValueLabel;

    @FXML
    private TextField bikeCostTextField;

    @FXML
    private TextField carCostTextField;

    @FXML
    private Button applyChangeButton;

    @FXML
    private Button backButton;

    private static String bikeCost;
    private static String carCost;

    @FXML
    public void initialize() {
        // Display the selected slider value in the Label initially
        sliderValueLabel.setText(String.valueOf((int) slotSlider.getValue()));

        // Add a listener to update the Label as the slider moves
        slotSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                sliderValueLabel.setText(String.valueOf(newValue.intValue()))
        );
        loadAvailableSlots();
        try {
            // Database connection
            String url = "jdbc:mysql://localhost:3306/smart_park";
            String username = "root";
            String password = "";

            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();

            // Fetch bike cost
            ResultSet resultSet = statement.executeQuery("SELECT cost FROM cost WHERE type = 'Bike'");
            if (resultSet.next()) {
                bikeCost = resultSet.getString("cost");
            }

            // Fetch car cost
            resultSet = statement.executeQuery("SELECT cost FROM cost WHERE type = 'Car'");
            if (resultSet.next()) {
                carCost = resultSet.getString("cost");
            }

            // Set the values in the text fields
            bikeCostTextField.setText(bikeCost);
            carCostTextField.setText(carCost);

            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error occurred while loading settings.", Alert.AlertType.ERROR);
        }
    }
    private void loadAvailableSlots() {
        try {
            // Database connection
            String url = "jdbc:mysql://localhost:3306/smart_park";
            String username = "root";
            String password = "";

            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();

            // Fetch the available slot IDs
            String query = "SELECT slotID FROM slots WHERE availability = 'available'";
            ResultSet resultSet = statement.executeQuery(query);

            // Clear the ChoiceBox and add the slot IDs to it
            slotChoiceBox.getItems().clear();
            while (resultSet.next()) {
                int slotId = resultSet.getInt("slotID");
                slotChoiceBox.getItems().add(slotId);
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error occurred while loading available slots.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void ApplyChange() {
        try {
            int numSlotsToAdd = (int) slotSlider.getValue();

            // Retrieve the cost values from the text fields
            String bikeCostValue = bikeCostTextField.getText();
            String carCostValue = carCostTextField.getText();

            // Validate that the cost values are valid numbers
            try {
                double bikeCost = Double.parseDouble(bikeCostValue);
                double carCost = Double.parseDouble(carCostValue);

                // Database connection details
                String url = "jdbc:mysql://localhost:3306/smart_park";
                String username = "root";
                String password = "";

                Connection connection = DriverManager.getConnection(url, username, password);
                connection.setAutoCommit(false);

                // Update costs for Bike and Car
                PreparedStatement updateBikeCostStatement = connection.prepareStatement("UPDATE cost SET cost = ? WHERE type = 'Bike'");
                PreparedStatement updateCarCostStatement = connection.prepareStatement("UPDATE cost SET cost = ? WHERE type = 'Car'");
                updateBikeCostStatement.setDouble(1, bikeCost);
                updateCarCostStatement.setDouble(1, carCost);
                updateBikeCostStatement.executeUpdate();
                updateCarCostStatement.executeUpdate();

                // Check current number of slots for selected type
                String checkSlotsQuery = "SELECT COUNT(*) FROM slots";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(checkSlotsQuery);
                resultSet.next();
                int currentSlotCount = resultSet.getInt(1);

                int slotsToCreate = numSlotsToAdd - currentSlotCount;

                if (slotsToCreate > 0) {
                    // Add new slots
                    PreparedStatement insertSlotStatement = connection.prepareStatement(
                            "INSERT INTO slots (availability, vehicleID) VALUES ('available', NULL)"
                    );

                    for (int i = 0; i < slotsToCreate; i++) {
                        insertSlotStatement.addBatch();
                    }
                    insertSlotStatement.executeBatch();
                    insertSlotStatement.close();
                }

                // Handle slot removal
                Integer selectedSlotId = slotChoiceBox.getValue();
                if (selectedSlotId != null) {
                    // Remove the selected slot
                    PreparedStatement removeSlotStatement = connection.prepareStatement(
                            "DELETE FROM slots WHERE slotID = ?"
                    );
                    removeSlotStatement.setInt(1, selectedSlotId);
                    removeSlotStatement.executeUpdate();
                    removeSlotStatement.close();
                }

                connection.commit();
                showAlert("Success", "Changes applied successfully!", Alert.AlertType.INFORMATION);

                // Clean up resources
                updateBikeCostStatement.close();
                updateCarCostStatement.close();
                statement.close();
                resultSet.close();
                connection.close();

            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter valid numeric values for costs.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error occurred while updating the costs.", Alert.AlertType.ERROR);
        }
        switchToAdmin();
    }

    @FXML
    private void switchToAdmin(){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/adminHome.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage=(Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go to SETTINGS.", Alert.AlertType.ERROR);
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

