package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.repository.Neo4jConnection;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            // 1️⃣ Test Neo4j connection before loading UI
            Neo4jConnection connection = Neo4jConnection.getInstance();
            if (!connection.testConnection()) {
                System.err.println("❌ Neo4j connection failed!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("Cannot connect to Neo4j. Please check your configuration.");
                alert.showAndWait();
                return;
            }
            System.out.println("✅ Neo4j connected successfully!");

            // 2️⃣ Load FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MainWindow.fxml")
            );

            Scene scene = new Scene(loader.load(), 1400, 800);

            // 3️⃣ Load CSS
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );

            // 4️⃣ Configure stage
            primaryStage.setTitle("TicketPro - Ticket Management System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(700);

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading application: " + e.getMessage());
        }
    }
}
