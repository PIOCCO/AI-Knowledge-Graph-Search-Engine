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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> colCategoryId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;
    @FXML private TableColumn<Category, String> colColor;
    @FXML private TableColumn<Category, Integer> colTicketCount;
    @FXML private TableColumn<Category, Void> colActions;

    @FXML private TextField searchField;

    private ObservableList<Category> categoryList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSampleCategories();
        setupTable();
    }

    private void loadSampleCategories() {
        categoryList = FXCollections.observableArrayList(
                new Category("CAT001", "Technical", "Technical issues and bugs", "#e74c3c", 45),
                new Category("CAT002", "Payment", "Payment and billing issues", "#f39c12", 12),
                new Category("CAT003", "UI/UX", "User interface problems", "#3498db", 18),
                new Category("CAT004", "Email", "Email and notification issues", "#9b59b6", 8),
                new Category("CAT005", "Database", "Database related problems", "#e67e22", 15),
                new Category("CAT006", "Security", "Security concerns", "#c0392b", 6),
                new Category("CAT007", "Performance", "Performance issues", "#16a085", 22),
                new Category("CAT008", "Feature", "Feature requests", "#27ae60", 31),
                new Category("CAT009", "Bug", "Software bugs", "#d35400", 28),
                new Category("CAT010", "Other", "Other issues", "#95a5a6", 10)
        );
    }

    private void setupTable() {
        colCategoryId.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colTicketCount.setCellValueFactory(new PropertyValueFactory<>("ticketCount"));

        // Color cell with preview
        colColor.setCellFactory(col -> new TableCell<Category, String>() {
            @Override
            protected void updateItem(String colorHex, boolean empty) {
                super.updateItem(colorHex, empty);
                if (empty || colorHex == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Circle circle = new Circle(10);
                    circle.setFill(Color.web(colorHex));
                    circle.setStroke(Color.gray(0.5));

                    Label label = new Label(colorHex);
                    label.setStyle("-fx-font-size: 11px;");

                    HBox box = new HBox(8, circle, label);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Ticket count with badge
        colTicketCount.setCellFactory(col -> new TableCell<Category, Integer>() {
            @Override
            protected void updateItem(Integer count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(String.valueOf(count));
                    badge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                            "-fx-background-radius: 12px; -fx-padding: 4px 12px; " +
                            "-fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<Category, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                editBtn.setTooltip(new Tooltip("Edit Category"));
                deleteBtn.setTooltip(new Tooltip("Delete Category"));

                editBtn.setOnAction(e -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleEditCategory(category);
                });

                deleteBtn.setOnAction(e -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        categoryTable.setItems(categoryList);
    }

    @FXML
    private void handleAddCategory() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText("Create a new ticket category");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");

        TextField descField = new TextField();
        descField.setPromptText("Description");

        ColorPicker colorPicker = new ColorPicker(Color.web("#3498db"));

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(colorPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (!nameField.getText().trim().isEmpty()) {
                    String id = "CAT" + String.format("%03d", categoryList.size() + 1);
                    String colorHex = String.format("#%02X%02X%02X",
                            (int) (colorPicker.getValue().getRed() * 255),
                            (int) (colorPicker.getValue().getGreen() * 255),
                            (int) (colorPicker.getValue().getBlue() * 255));

                    return new Category(id, nameField.getText(), descField.getText(), colorHex, 0);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(category -> {
            categoryList.add(category);
            showAlert("Success", "Category created successfully!", Alert.AlertType.INFORMATION);
        });
    }

    @FXML
    private void handleRefresh() {
        categoryTable.refresh();
        showAlert("Refresh", "Category list refreshed", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            categoryTable.setItems(categoryList);
        } else {
            ObservableList<Category> filtered = categoryList.filtered(cat ->
                    cat.getName().toLowerCase().contains(query) ||
                            cat.getDescription().toLowerCase().contains(query)
            );
            categoryTable.setItems(filtered);
        }
    }

    private void handleEditCategory(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit category: " + category.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(category.getName());
        TextField descField = new TextField(category.getDescription());
        ColorPicker colorPicker = new ColorPicker(Color.web(category.getColor()));

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(colorPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                category.setName(nameField.getText());
                category.setDescription(descField.getText());
                String colorHex = String.format("#%02X%02X%02X",
                        (int) (colorPicker.getValue().getRed() * 255),
                        (int) (colorPicker.getValue().getGreen() * 255),
                        (int) (colorPicker.getValue().getBlue() * 255));
                category.setColor(colorHex);
                return category;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            categoryTable.refresh();
            showAlert("Success", "Category updated successfully!", Alert.AlertType.INFORMATION);
        });
    }

    private void handleDeleteCategory(Category category) {
        if (category.getTicketCount() > 0) {
            showAlert("Cannot Delete",
                    "This category has " + category.getTicketCount() + " tickets. " +
                            "Please reassign or close these tickets first.",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete category: " + category.getName() + "?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                categoryList.remove(category);
                showAlert("Success", "Category deleted successfully!", Alert.AlertType.INFORMATION);
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

    // Category Model Class
    public static class Category {
        private final String categoryId;
        private String name;
        private String description;
        private String color;
        private int ticketCount;

        public Category(String categoryId, String name, String description,
                        String color, int ticketCount) {
            this.categoryId = categoryId;
            this.name = name;
            this.description = description;
            this.color = color;
            this.ticketCount = ticketCount;
        }

        public String getCategoryId() { return categoryId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public int getTicketCount() { return ticketCount; }
        public void setTicketCount(int ticketCount) { this.ticketCount = ticketCount; }
    }
}