package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class userHome {
    @FXML
    private Button logoutbutton;

    @FXML
    private void switchToLogin(){
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

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert=new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
