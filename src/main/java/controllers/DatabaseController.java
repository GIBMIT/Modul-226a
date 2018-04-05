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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DatabaseController extends AppController implements Initializable {

    @FXML
    private Text databasename;

    @FXML
    public ListView<String> tablesList;

    @FXML
    public Label error;

    public ContextMenu contextMenu = new ContextMenu();


    public void createTable() {

    }

    public void changeDatabase() throws IOException {
        this.initStartGUI();
    }

    public void editTable() {
        String name = this.tablesList.selectionModelProperty().get().getSelectedItem();
        Table table = new Table(this.getDatabase(), name);
        try {
            table.getColumns();
            this.setTable(table);

        } catch (QueryFailedException e) {
            // todo set error
            e.printStackTrace();
        }
    }

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

    public void initialize(URL location, ResourceBundle resources) {
        this.error.setText("");
        Database database = this.getDatabase();
        this.databasename.setText(database.getDatabase());
        MainTable model = new MainTable(database);
        ArrayList<String> tablenames = null;
        try {
            tablenames = model.getTables();
        } catch (QueryFailedException e) {
            e.printStackTrace();
        }
        ObservableList<String> tables = tablesList.getItems();
        tables.removeAll();
        tables.addAll(tablenames);
        this.createContectMenu();
    }

    private void createContectMenu() {
        MenuItem edit = new MenuItem("Edit");
        MenuItem view = new MenuItem("View");
        edit.setOnAction(event -> {
            this.editTable();
        });
        view.setOnAction(event -> {
            this.viewTable();
        });
        this.contextMenu.getItems().addAll(edit, view);
        this.tablesList.setContextMenu(this.contextMenu);
    }
}
