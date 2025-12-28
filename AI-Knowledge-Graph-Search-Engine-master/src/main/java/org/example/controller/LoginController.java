package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private CheckBox chkRememberMe;
    @FXML private Label lblError;
    @FXML private ProgressIndicator progressIndicator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Hide error label and progress indicator initially
        lblError.setVisible(false);
        progressIndicator.setVisible(false);

        // Add enter key support
        txtPassword.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Clear previous error
        lblError.setVisible(false);

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        // Show loading
        progressIndicator.setVisible(true);
        btnLogin.setDisable(true);

        // Simulate authentication (replace with real authentication)
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate network delay

                // Demo credentials
                boolean isValid = authenticateUser(username, password);

                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    btnLogin.setDisable(false);

                    if (isValid) {
                        // Store user session
                        SessionManager.getInstance().setCurrentUser(username);

                        // Navigate to main window
                        navigateToMainWindow();
                    } else {
                        showError("Invalid username or password");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean authenticateUser(String username, String password) {
        // Demo authentication - replace with database/API call
        return (username.equals("admin") && password.equals("admin")) ||
                (username.equals("user") && password.equals("user"));
    }

    private void navigateToMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TicketPro - Dashboard");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading main window: " + e.getMessage());
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Reset");
        alert.setContentText("Please contact your administrator to reset your password.");
        alert.showAndWait();
    }
}

// Session Manager Class
class SessionManager {
    private static SessionManager instance;
    private String currentUser;
    private String userRole;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
        this.userRole = username.equals("admin") ? "Administrator" : "User";
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public String getUserRole() {
        return userRole;
    }

    public void logout() {
        currentUser = null;
        userRole = null;
    }
}