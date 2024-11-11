package controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class login{
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink signUpLink;

    @FXML
    private void handleLogin(){
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }
        String role = validateLogin(username, password);
        if (role != null) {
            SessionManager.getInstance().setUsername(username);
            SessionManager.getInstance().setRole(role);
            showAlert("Success", "Login successful! Role: " + role, Alert.AlertType.INFORMATION);
            if (role.equals("admin")) {
                switchToAdminHome();  // Switch to admin home
            } else {
                switchToUserHome();  // Switch to user home
            }
        } else {
            showAlert("Error", "Invalid username or password.", Alert.AlertType.ERROR);
        }
    }

    private String validateLogin(String username, String password) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "")) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void switchToUserHome() {
        // Load the user home page (userHome.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userHome.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);  // Set the new scene for the user
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the user home screen.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void switchToAdminHome() {
        // Load the admin home page (adminHome.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/adminHome.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);  // Set the new scene for the admin
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the admin home screen.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void switchToSignin(){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/signin.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage=(Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the signin screen.", Alert.AlertType.ERROR);
        }
    }
}

