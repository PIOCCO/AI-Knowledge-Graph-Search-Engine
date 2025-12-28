package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colJoinedDate;
    @FXML private TableColumn<User, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;

    private ObservableList<User> userList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadSampleUsers();
        setupTable();
    }

    private void setupFilters() {
        roleFilter.setItems(FXCollections.observableArrayList(
                "All Roles", "Administrator", "Agent", "User"
        ));
        roleFilter.setValue("All Roles");

        statusFilter.setItems(FXCollections.observableArrayList(
                "All Status", "Active", "Inactive", "Suspended"
        ));
        statusFilter.setValue("All Status");
    }

    private void loadSampleUsers() {
        userList = FXCollections.observableArrayList(
                new User("U001", "admin", "admin@ticketpro.com", "Administrator", "Active", "2024-01-15"),
                new User("U002", "john.doe", "john@ticketpro.com", "Agent", "Active", "2024-02-20"),
                new User("U003", "jane.smith", "jane@ticketpro.com", "Agent", "Active", "2024-03-10"),
                new User("U004", "mike.johnson", "mike@ticketpro.com", "User", "Active", "2024-04-05"),
                new User("U005", "sarah.wilson", "sarah@ticketpro.com", "Agent", "Active", "2024-05-12"),
                new User("U006", "bob.miller", "bob@ticketpro.com", "User", "Inactive", "2024-06-18")
        );
    }

    private void setupTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colJoinedDate.setCellValueFactory(new PropertyValueFactory<>("joinedDate"));

        // Status cell with colored badges
        colStatus.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-background-radius: 12px; -fx-padding: 4px 12px; " +
                            "-fx-font-size: 11px; -fx-font-weight: bold;");

                    switch (status) {
                        case "Active":
                            label.setStyle(label.getStyle() + "-fx-background-color: #d1e7dd; -fx-text-fill: #0f5132;");
                            break;
                        case "Inactive":
                            label.setStyle(label.getStyle() + "-fx-background-color: #e2e3e5; -fx-text-fill: #41464b;");
                            break;
                        case "Suspended":
                            label.setStyle(label.getStyle() + "-fx-background-color: #f8d7da; -fx-text-fill: #842029;");
                            break;
                    }

                    setGraphic(label);
                    setText(null);
                }
            }
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button resetBtn = new Button("🔑");

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                resetBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                editBtn.setTooltip(new Tooltip("Edit User"));
                deleteBtn.setTooltip(new Tooltip("Delete User"));
                resetBtn.setTooltip(new Tooltip("Reset Password"));

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });

                resetBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleResetPassword(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editBtn, resetBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        userTable.setItems(userList);
    }

    @FXML
    private void handleAddUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");
        dialog.setContentText("Username:");

        dialog.showAndWait().ifPresent(username -> {
            if (!username.trim().isEmpty()) {
                String userId = "U" + String.format("%03d", userList.size() + 1);
                User newUser = new User(userId, username, username + "@ticketpro.com",
                        "User", "Active", "2024-12-07");
                userList.add(newUser);
                showAlert("Success", "User created successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    private void handleRefresh() {
        userTable.refresh();
        showAlert("Refresh", "User list refreshed", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            userTable.setItems(userList);
        } else {
            ObservableList<User> filtered = userList.filtered(user ->
                    user.getUsername().toLowerCase().contains(query) ||
                            user.getEmail().toLowerCase().contains(query)
            );
            userTable.setItems(filtered);
        }
    }

    private void handleEditUser(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Administrator", "Agent", "User"
        ));
        roleCombo.setValue(user.getRole());

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Active", "Inactive", "Suspended"
        ));
        statusCombo.setValue(user.getStatus());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Role:"), 0, 0);
        grid.add(roleCombo, 1, 0);
        grid.add(new Label("Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setRole(roleCombo.getValue());
                user.setStatus(statusCombo.getValue());
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            userTable.refresh();
            showAlert("Success", "User updated successfully!", Alert.AlertType.INFORMATION);
        });
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete user: " + user.getUsername() + "?");
        alert.setContentText("This action cannot be undone. All user data will be permanently deleted.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userList.remove(user);
                showAlert("Success", "User deleted successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void handleResetPassword(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Password");
        alert.setHeaderText("Reset password for: " + user.getUsername() + "?");
        alert.setContentText("A temporary password will be sent to: " + user.getEmail());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert("Success", "Password reset email sent to " + user.getEmail(),
                        Alert.AlertType.INFORMATION);
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // User Model Class
    public static class User {
        private final String userId;
        private final String username;
        private final String email;
        private String role;
        private String status;
        private final String joinedDate;

        public User(String userId, String username, String email, String role,
                    String status, String joinedDate) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.status = status;
            this.joinedDate = joinedDate;
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getJoinedDate() { return joinedDate; }
    }
}