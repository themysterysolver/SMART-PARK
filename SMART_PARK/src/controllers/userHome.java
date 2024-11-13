package controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Comparator;

public class userHome {
    @FXML
    private Button logoutbutton;
    @FXML
    private Button historyButton;  // Button to open transaction history

    @FXML
    public TableView statusTableView;
    @FXML
    private AnchorPane LHS;  // AnchorPane for the left side

    @FXML
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

                }

                // Add the button to the TilePane
                slotTilePane.getChildren().add(slotButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch slot data from the database.", Alert.AlertType.ERROR);
        }
    }
    @FXML
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
    private void showHistoryDialog() {
        // Get userID from session (use the username to query the database for userID)
        String username = SessionManager.getInstance().getUsername();  // Assuming username is stored in the session
        String userID = getUserIDFromUsername(username);

        if (userID != null) {
            // Create a new Stage (Dialog) for the history
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Transaction History");

            // Create TableView for transactions
            TableView<TransactionRecord> transactionTable = new TableView<>();
            TableColumn<TransactionRecord, Integer> transactionIDColumn = new TableColumn<>("Transaction ID");
            transactionIDColumn.setCellValueFactory(cellData -> cellData.getValue().transactionIDProperty().asObject());

            TableColumn<TransactionRecord, Integer> slotIDColumn = new TableColumn<>("Slot ID");
            slotIDColumn.setCellValueFactory(cellData -> cellData.getValue().slotIDProperty().asObject());

            TableColumn<TransactionRecord, String> startDateColumn = new TableColumn<>("Start Date");
            startDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());

            TableColumn<TransactionRecord, String> endDateColumn = new TableColumn<>("End Date");
            endDateColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());

            TableColumn<TransactionRecord, Double> costColumn = new TableColumn<>("Cost");
            costColumn.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());

            TableColumn<TransactionRecord, String> typeColumn = new TableColumn<>("Type");
            typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());

            // Add columns to the table
            transactionTable.getColumns().addAll(transactionIDColumn, slotIDColumn, startDateColumn, endDateColumn, costColumn, typeColumn);

            // Fetch transaction data
            ObservableList<TransactionRecord> transactionList = fetchTransactionHistory(userID);

            // Add data to the table
            transactionTable.setItems(transactionList);

            // Sort Button
            Button sortButton = new Button("Sort by Date");
            sortButton.setOnAction(e -> {
                FXCollections.sort(transactionList, Comparator.comparing(TransactionRecord::getStartDate));
                // Toggle sorting order between ascending and descending
                FXCollections.reverse(transactionList);
            });

            VBox layout = new VBox(10, transactionTable, sortButton);
            dialog.setScene(new Scene(layout, 600, 400));
            dialog.show();
        } else {
            showAlert("Error", "User not found.", Alert.AlertType.ERROR);
        }
    }
    private String getUserIDFromUsername(String username) {
        String userID = null;
        String query = "SELECT userid FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_park", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    userID = rs.getString("userid");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch userID from the database.", Alert.AlertType.ERROR);
        }

        return userID;
    }
    private ObservableList<TransactionRecord> fetchTransactionHistory(String userID) {
        ObservableList<TransactionRecord> transactionList = FXCollections.observableArrayList();

        String url = "jdbc:mysql://localhost:3306/smart_park";
        String user = "root";
        String password = "";

        String query = "SELECT transactionID, slotID, startDate, endDate, cost, type FROM transactions WHERE userID = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int transactionID = rs.getInt("transactionID");
                    int slotID = rs.getInt("slotID");
                    String startDate = rs.getString("startDate");
                    String endDate = rs.getString("endDate");
                    double cost = rs.getDouble("cost");
                    String type = rs.getString("type");

                    transactionList.add(new TransactionRecord(transactionID, slotID, startDate, endDate, cost, type));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch transaction history.", Alert.AlertType.ERROR);
        }

        return transactionList;
    }


    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

