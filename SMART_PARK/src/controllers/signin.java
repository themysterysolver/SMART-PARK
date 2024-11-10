package controllers;

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
import java.sql.SQLException;

public class signin {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField pwdField;
    @FXML
    private PasswordField repwdField;
    @FXML
    private Button signinbuttonField;
    @FXML
    private Hyperlink loginlinkField;


    @FXML
    private void handleSignIn(){
        String username=usernameField.getText();
        String pwd=pwdField.getText();
        String repwd=repwdField.getText();
        if(!pwd.equals(repwd)) {
            showAlert("Password Mismatch", "Passwords do not match!", Alert.AlertType.ERROR);
            return;
        }
        else{
          try(Connection conn= DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "")){
                String insertSQL="INSERT INTO USERS (username,password,type) VALUES(?,?,'user')";
              PreparedStatement stmt=conn.prepareStatement(insertSQL);
              stmt.setString(1,username);
              stmt.setString(2,pwd);
              int rowsaffected=stmt.executeUpdate();
              if(rowsaffected>0){
                  showAlert("Sucess","user signed sucessfully!", Alert.AlertType.INFORMATION);
                  usernameField.clear();
                  pwdField.clear();
                  repwdField.clear();
                  switchToLogin();
              }
              else{
                  showAlert("Error","Could not sign up.Try again Later.", Alert.AlertType.ERROR);
              }
          } catch (SQLException e) {
              e.printStackTrace();
              showAlert("Error", "Database connection failed.", Alert.AlertType.ERROR);
          }
        }

        }
      private void showAlert(String title, String content, Alert.AlertType alertType){
         Alert alert=new Alert(alertType);
         alert.setTitle(title);
         alert.setHeaderText(null);
         alert.setContentText(content);
         alert.showAndWait();
      }
      @FXML
      private void switchToLogin(){
          FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
          try {
              Parent root = loader.load();
              Scene scene = new Scene(root);
              Stage stage=(Stage) loginlinkField.getScene().getWindow();
              stage.setScene(scene);
          } catch (IOException e) {
              e.printStackTrace();
              showAlert("Error", "Failed to load the login screen.", Alert.AlertType.ERROR);
          }
      }
    }



