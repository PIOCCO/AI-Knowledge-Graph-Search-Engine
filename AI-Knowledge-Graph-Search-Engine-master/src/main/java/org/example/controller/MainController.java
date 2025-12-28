package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;

import org.example.model.Ticket;
import org.example.repository.TicketRepository;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Sidebar Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnTickets;
    @FXML private Button btnNewTicket;
    @FXML private Button btnCategories;
    @FXML private Button btnUsers;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;

    // Header Elements
    @FXML private Label lblPageTitle;
    @FXML private TextField searchField;
    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;

    // Dashboard Stats
    @FXML private Label lblTotalTickets;
    @FXML private Label lblOpenTickets;
    @FXML private Label lblProgressTickets;
    @FXML private Label lblResolvedTickets;

    // Dashboard View
    @FXML private VBox dashboardView;
    @FXML private TableView<Ticket> recentTicketsTable;
    @FXML private TableColumn<Ticket, String> colTicketId;
    @FXML private TableColumn<Ticket, String> colTitle;
    @FXML private TableColumn<Ticket, String> colStatus;
    @FXML private TableColumn<Ticket, String> colPriority;
    @FXML private TableColumn<Ticket, String> colCategory;
    @FXML private TableColumn<Ticket, String> colAssignedTo;
    @FXML private TableColumn<Ticket, String> colCreatedAt;

    // Tickets View
    @FXML private VBox ticketsView;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private TableView<Ticket> allTicketsTable;
    @FXML private TableColumn<Ticket, String> colAllTicketId;
    @FXML private TableColumn<Ticket, String> colAllTitle;
    @FXML private TableColumn<Ticket, String> colAllStatus;
    @FXML private TableColumn<Ticket, String> colAllPriority;
    @FXML private TableColumn<Ticket, String> colAllCategory;
    @FXML private TableColumn<Ticket, String> colAllAssignedTo;
    @FXML private TableColumn<Ticket, String> colAllCreatedAt;
    @FXML private TableColumn<Ticket, Void> colAllActions;

    private Button activeButton;
    private ObservableList<Ticket> ticketList;
    private TicketRepository ticketRepository;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize repository
        ticketRepository = new TicketRepository();

        // Set active button
        setActiveButton(btnDashboard);

        // Setup user info
        lblUsername.setText("Admin User");
        lblUserRole.setText("Administrator");

        // Setup filters
        setupFilters();

        // Load data from Neo4j
        loadTicketsFromDatabase();

        // Setup tables
        setupRecentTicketsTable();
        setupAllTicketsTable();

        // Load stats
        updateDashboardStats();

        // Show dashboard by default
        showDashboard();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Open", "In Progress", "Resolved", "Closed"
        ));
        statusFilter.setValue("All");

        priorityFilter.setItems(FXCollections.observableArrayList(
                "All", "Low", "Medium", "High", "Critical"
        ));
        priorityFilter.setValue("All");
    }

    private void loadTicketsFromDatabase() {
        try {
            List<Ticket> tickets = ticketRepository.findAll();
            ticketList = FXCollections.observableArrayList(tickets);
            System.out.println("✅ Loaded " + tickets.size() + " tickets from Neo4j");
        } catch (Exception e) {
            System.err.println("❌ Error loading tickets: " + e.getMessage());
            e.printStackTrace();
            // Fallback to empty list
            ticketList = FXCollections.observableArrayList();
        }
    }

    private void setupRecentTicketsTable() {
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAssignedTo.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAtFormatted"));

        // Apply custom cell factories for styling
        colStatus.setCellFactory(col -> createStatusCell());
        colPriority.setCellFactory(col -> createPriorityCell());

        // Load recent tickets (first 5)
        recentTicketsTable.setItems(FXCollections.observableArrayList(
                ticketList.subList(0, Math.min(5, ticketList.size()))
        ));
    }

    private void setupAllTicketsTable() {
        colAllTicketId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAllTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAllStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAllPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colAllCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAllAssignedTo.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
        colAllCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAtFormatted"));

        // Apply custom cell factories
        colAllStatus.setCellFactory(col -> createStatusCell());
        colAllPriority.setCellFactory(col -> createPriorityCell());

        // Actions column
        colAllActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleViewTicket(ticket);
                });

                editBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleEditTicket(ticket);
                });

                deleteBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleDeleteTicket(ticket);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, viewBtn, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        allTicketsTable.setItems(ticketList);
    }

    private TableCell<Ticket, String> createStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.getStyleClass().add("status-badge");

                    switch (status) {
                        case "Open":
                            label.getStyleClass().add("status-open");
                            break;
                        case "In Progress":
                            label.getStyleClass().add("status-progress");
                            break;
                        case "Resolved":
                            label.getStyleClass().add("status-resolved");
                            break;
                        case "Closed":
                            label.getStyleClass().add("status-closed");
                            break;
                    }

                    setGraphic(label);
                    setText(null);
                }
            }
        };
    }

    private TableCell<Ticket, String> createPriorityCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);

                    switch (priority) {
                        case "Low":
                            setStyle("-fx-text-fill: #0f5132;");
                            break;
                        case "Medium":
                            setStyle("-fx-text-fill: #664d03; -fx-font-weight: bold;");
                            break;
                        case "High":
                            setStyle("-fx-text-fill: #842029; -fx-font-weight: bold;");
                            break;
                        case "Critical":
                            setStyle("-fx-text-fill: #721c24; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        };
    }

    private void updateDashboardStats() {
        long total = ticketList.size();
        long open = ticketList.stream().filter(t -> "Open".equals(t.getStatus())).count();
        long progress = ticketList.stream().filter(t -> "In Progress".equals(t.getStatus())).count();
        long resolved = ticketList.stream().filter(t -> "Resolved".equals(t.getStatus())).count();

        lblTotalTickets.setText(String.valueOf(total));
        lblOpenTickets.setText(String.valueOf(open));
        lblProgressTickets.setText(String.valueOf(progress));
        lblResolvedTickets.setText(String.valueOf(resolved));
    }

    // Navigation Handlers
    @FXML
    private void handleDashboard() {
        setActiveButton(btnDashboard);
        showDashboard();
        lblPageTitle.setText("Dashboard");
    }

    @FXML
    private void handleTickets() {
        setActiveButton(btnTickets);
        showTicketsView();
        lblPageTitle.setText("All Tickets");
    }

    @FXML
    private void handleNewTicket() {
        openTicketForm(null);
    }

    @FXML
    private void handleCategories() {
        setActiveButton(btnCategories);
        lblPageTitle.setText("Categories");
        openCategoryManagement();
    }

    @FXML
    private void handleUsers() {
        setActiveButton(btnUsers);
        lblPageTitle.setText("Users");
        openUserManagement();
    }

    @FXML
    private void handleSettings() {
        setActiveButton(btnSettings);
        lblPageTitle.setText("Settings");
        showAlert("Settings", "Settings panel coming soon", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login page.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Logout confirmed");
                // TODO: Redirect to login page
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (!query.isEmpty()) {
            showAlert("Search", "Searching for: " + query, Alert.AlertType.INFORMATION);
            // TODO: Implement search functionality
        }
    }

    @FXML
    private void handleViewAllTickets() {
        handleTickets();
    }

    @FXML
    private void handleRefresh() {
        loadTicketsFromDatabase();
        allTicketsTable.refresh();
        recentTicketsTable.refresh();
        updateDashboardStats();
        showAlert("Refresh", "Ticket list refreshed from database", Alert.AlertType.INFORMATION);
    }

    private void handleViewTicket(Ticket ticket) {
        showAlert("View Ticket", "Viewing ticket: " + ticket.getId() + "\n" + ticket.getTitle(),
                Alert.AlertType.INFORMATION);
    }

    private void handleEditTicket(Ticket ticket) {
        openTicketForm(ticket);
    }

    private void handleDeleteTicket(Ticket ticket) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Ticket");
        alert.setHeaderText("Delete " + ticket.getId() + "?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = ticketRepository.delete(ticket.getId());
                if (success) {
                    ticketList.remove(ticket);
                    updateDashboardStats();
                    showAlert("Success", "Ticket deleted successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to delete ticket", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // View Management
    private void showDashboard() {
        dashboardView.setVisible(true);
        ticketsView.setVisible(false);
    }

    private void showTicketsView() {
        dashboardView.setVisible(false);
        ticketsView.setVisible(true);
    }

    // Helper Methods
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        activeButton = button;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Methods to open other windows
    private void openTicketForm(Ticket ticket) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TicketForm.fxml"));
            javafx.scene.Parent root = loader.load();

            TicketFormController controller = loader.getController();

            if (ticket != null) {
                controller.setEditMode(ticket);
            }

            controller.setSaveCallback((savedTicket, isEdit) -> {
                if (isEdit) {
                    // Update ticket in database
                    Ticket updated = ticketRepository.update(savedTicket);
                    if (updated != null) {
                        int index = ticketList.indexOf(ticket);
                        if (index >= 0) {
                            ticketList.set(index, savedTicket);
                        }
                    }
                } else {
                    // Create new ticket in database
                    Ticket created = ticketRepository.create(savedTicket);
                    if (created != null) {
                        ticketList.add(0, created);
                    }
                }
                updateDashboardStats();
                recentTicketsTable.refresh();
                allTicketsTable.refresh();
            });

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(ticket == null ? "Create New Ticket" : "Edit Ticket");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open ticket form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserManagement.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("User Management");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open user management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openCategoryManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryManagement.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Category Management");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open category management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}