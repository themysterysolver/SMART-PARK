package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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

    private static String bikeCost = "15";
    private static String carCost = "100";

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
        bikeCostTextField.setText(bikeCost);
        carCostTextField.setText(carCost);
    }
    @FXML
    public void ApplyChange(){
        try {
            bikeCost = (bikeCostTextField.getText());
            carCost = (carCostTextField.getText());
            bikeCostTextField.setText(bikeCost);
            carCostTextField.setText(carCost);
            showAlert("Success", "Changes applied successfully!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for costs.", Alert.AlertType.ERROR);
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

