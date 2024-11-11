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
    private ChoiceBox<String> typeChoiceBox;

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
        // Populate ChoiceBox with "Bike" and "Car" options
        typeChoiceBox.getItems().addAll("Bike", "Car");

        // Display the selected slider value in the Label initially
        sliderValueLabel.setText(String.valueOf((int) slotSlider.getValue()));

        // Add a listener to update the Label as the slider moves
        slotSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                sliderValueLabel.setText(String.valueOf(newValue.intValue()))
        );
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
    @FXML
    public void ApplyChange(){
        try {
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
                PreparedStatement updateBikeCostStatement = connection.prepareStatement("UPDATE cost SET cost = ? WHERE type = 'Bike'");
                PreparedStatement updateCarCostStatement = connection.prepareStatement("UPDATE cost SET cost = ? WHERE type = 'Car'");

                // Set the new cost values for Bike and Car
                updateBikeCostStatement.setDouble(1, bikeCost);
                updateCarCostStatement.setDouble(1, carCost);

                // Execute the update statements
                int bikeRowsUpdated = updateBikeCostStatement.executeUpdate();
                int carRowsUpdated = updateCarCostStatement.executeUpdate();

                if (bikeRowsUpdated > 0 && carRowsUpdated > 0) {
                    // Successfully updated the costs in the database
                    showAlert("Success", "Changes applied successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to apply changes.", Alert.AlertType.ERROR);
                }

                // Clean up resources
                updateBikeCostStatement.close();
                updateCarCostStatement.close();
                connection.close();

            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter valid numeric values for costs.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error occurred while updating the costs.", Alert.AlertType.ERROR);
        }
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

