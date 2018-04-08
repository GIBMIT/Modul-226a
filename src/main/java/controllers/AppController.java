package controllers;

import exception.TableNotFoundException;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.schema.Database;
import exception.DatabaseNotFoundException;
import models.schema.Table;
import services.Container;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Abstract class AppController
 */
class AppController {

    private boolean edited = false;

    private static Stage stage;

    private static Database database;

    private static Table table;

    /**
     * Discard all changes
     */
    @FXML
    public void discardChanges() {
        try {
            if (this.edited == false) {
                this.initDatabaseGUI();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm");
            alert.setHeaderText("Discard Changes");
            alert.setContentText("All changes are not saved. Are you ok with this?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                this.initDatabaseGUI();
            }
        } catch (IOException | DatabaseNotFoundException e) {
            // this should never be catched.
            e.printStackTrace();
        }
    }

    /**
     * Set cell value factory
     * @param j int Indicator, which column should be used
     * @param col TableColumn
     */
    static void setCellValueFactory(int j, TableColumn col) {
        col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> {
            List rowValues = param.getValue();
            String cellValue;
            // When a value does not exist (array size to small), set an empty value
            if (j < rowValues.size()) {
                if (rowValues.get(j) instanceof Integer) {
                    cellValue = Integer.toString((Integer) rowValues.get(j));
                } else {
                    try {
                        cellValue = rowValues.get(j).toString();
                    } catch (NullPointerException e) {
                        cellValue = "NULL";
                    }
                }
                if (cellValue == null) {
                    cellValue = "NULL";
                }
            } else {
                cellValue = "NULL";
            }
            return new SimpleObjectProperty<>(cellValue);
        });
    }

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

    /**
     * Set Table object
     *
     * @param t Table
     */
    void setTable(Table t) {
        Container c = Container.getInstance();
        c.set("Table", t);
        table = t;
    }

    /**
     * Get Table object
     *
     * @return Table table
     */
    Table getTable() {
        return table;
    }

    /**
     * Get Database object
     *
     * @return Database database
     */
    Database getDatabase() {
        return database;
    }

    /**
     * Init Database GUI
     *
     * @throws IOException               IOException
     * @throws DatabaseNotFoundException DatabaseNotFoundException
     */
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

    /**
     * Init Start GUI
     *
     * @throws IOException IOException
     */
    void initStartGUI() throws IOException {
        AnchorPane root = FXMLLoader.load(getClass().getResource("/views/startview.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Init table GUI
     *
     * @throws IOException            IOException
     * @throws TableNotFoundException TableNotFoundException
     */
    void initTableGUI() throws IOException, TableNotFoundException {
        if (this.getTable() == null) {
            throw new TableNotFoundException("Please select any table");
        }

        AnchorPane root = FXMLLoader.load(getClass().getResource("/views/tableview.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Init Edit GUI
     *
     * @throws TableNotFoundException TableNotFoundException
     * @throws IOException            IOException
     */
    void initEditGUI() throws TableNotFoundException, IOException {
        if (this.getTable() == null) {
            throw new TableNotFoundException("Please select any Table");
        }

        AnchorPane root = FXMLLoader.load(getClass().getResource("/views/createview.fxml"));
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
