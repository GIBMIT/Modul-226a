package controllers;

import exception.TableNotFoundException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import services.Database;
import exception.DatabaseNotFoundException;
import services.Table;

import java.io.IOException;

/**
 * Abstract class AppController
 */
class AppController {

    private static Stage stage;

    private static Database database;

    private static Table table;

    /**
     * Start the application
     *
     * @param primaryStage Stage
     */
    public void startApplication(Stage primaryStage) {
        stage = primaryStage;
        try {
            this.initStartGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setDatabase(Database db) {
        database = db;
    }

    void setTable(Table t) {
        table = t;
    }

    Table getTable() {
        return table;
    }

    Database getDatabase() {
        return database;
    }

    void initDatabaseGUI() throws IOException, DatabaseNotFoundException {
        if (database == null) {
            throw new DatabaseNotFoundException("Database not found");
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/databaseview.fxml"));

        AnchorPane anchorPane = loader.load();

        Scene scene = new Scene(anchorPane);
        stage.setScene(scene);
        stage.show();
    }

    void initStartGUI() throws IOException {
        AnchorPane root = FXMLLoader.load(getClass().getResource("/views/startview.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    void initTableGUI() throws IOException, TableNotFoundException {
        if (this.getTable() == null){
            throw new TableNotFoundException("Please select any table");
        }

        AnchorPane root = FXMLLoader.load(getClass().getResource("/views/tableview.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Set an error to a field
     *
     * @param field TextField
     */
    void setError(TextField field) {
        field.setStyle("-fx-border-color: red;");
    }
}
