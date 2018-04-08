package controllers;

import exception.QueryFailedException;
import exception.TableNotFoundException;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import models.tables.MainTable;
import models.schema.Database;
import models.schema.Table;
import services.Container;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class DatabaseController extends AppController implements Initializable {

    @FXML
    private Text databasename;

    @FXML
    public ListView<String> tablesList;

    @FXML
    public Label error;

    private ContextMenu contextMenu = new ContextMenu();

    /**
     * Create new table
     */
    public void createTable() {

        Table table = new Table(this.getDatabase(), "new Table");
        table.setIsCreated(false);
        this.setTable(table);
        try {
            this.initEditGUI();
        } catch (TableNotFoundException | IOException e) {
            this.error.setText("Sorry, something went wrong. Code 5");
            e.printStackTrace();
        }
    }

    /**
     * Change Database
     *
     * @throws IOException IOException
     */
    public void changeDatabase() throws IOException {
        this.initStartGUI();
    }

    /**
     * Edit selected table
     */
    public void editTable() {
        String name = this.tablesList.selectionModelProperty().get().getSelectedItem();
        Table table = new Table(this.getDatabase(), name);
        try {
            table.getColumns();
            this.setTable(table);
            this.initEditGUI();
        } catch (QueryFailedException | TableNotFoundException | IOException e) {
            this.error.setText("Sorry, this table is not editable");
            e.printStackTrace();
        }
    }

    /**
     * Delete table
     */
    public void deleteTable() {
        String name = this.tablesList.selectionModelProperty().get().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setContentText(String.format("Are you sure you want to delete the table '%s'?", name));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Database db = (Database) Container.getInstance().get("database");
            try {
                Statement stmt = db.getConnection().createStatement();
                stmt.executeUpdate(String.format("DROP TABLE %s", name));
                loadTables();
            } catch (SQLException e) {
                this.error.setText(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * View selected Table
     */
    public void viewTable() {
        this.error.setText("");
        String name = this.tablesList.selectionModelProperty().get().getSelectedItem();
        Table table = new Table(this.getDatabase(), name);
        try {
            table.getRows();
            this.setTable(table);
            this.initTableGUI();
        } catch (QueryFailedException | TableNotFoundException | IOException e) {
            this.error.setText("Sorry, this table is not readable");
            e.printStackTrace();
        }
    }

    /**
     * Initialize method
     *
     * @param location  URL
     * @param resources ResourceBundle
     */
    public void initialize(URL location, ResourceBundle resources) {
        loadTables();
        this.createContectMenu();
    }

    /**
     * Load all tables from database into list
     */
    private void loadTables() {
        this.error.setText("");
        Database database = this.getDatabase();
        this.databasename.setText(database.getDatabaseName());
        MainTable model = new MainTable(database);
        ArrayList<String> tablenames = null;
        tablenames = model.getTables();
        ObservableList<String> tables = tablesList.getItems();
        tables.clear();
        tables.addAll(tablenames);
        this.tablesList.refresh();
    }

    /**
     * Create context menu
     */
    private void createContectMenu() {
        MenuItem edit = new MenuItem("Edit");
        MenuItem view = new MenuItem("View");
        MenuItem delete = new MenuItem("Delete");

        edit.setOnAction(event -> this.editTable());
        view.setOnAction(event -> this.viewTable());
        delete.setOnAction(event -> this.deleteTable());

        this.contextMenu.getItems().addAll(edit, view, delete);
        this.tablesList.setContextMenu(this.contextMenu);
    }
}
