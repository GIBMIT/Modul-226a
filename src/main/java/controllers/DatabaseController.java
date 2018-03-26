package controllers;

import exception.QueryFailedException;
import exception.TableNotFoundException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import models.MainModel;
import services.Database;
import services.Table;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DatabaseController extends AppController implements Initializable {

    @FXML
    private Text databasename;

    @FXML
    public ListView<String> tablesList;

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
            table.getAttributes();
            this.setTable(table);

        } catch (QueryFailedException e) {
            // todo set error
            e.printStackTrace();
        }
    }

    public void viewTable() {
        String name = this.tablesList.selectionModelProperty().get().getSelectedItem();
        Table table = new Table(this.getDatabase(), name);
        try {
            table.getRows();
            this.setTable(table);
            this.initTableGUI();
        } catch (QueryFailedException | TableNotFoundException | IOException e) {
            // todo set error
            e.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        Database database = this.getDatabase();
        this.databasename.setText(database.getDatabase());
        MainModel model = new MainModel(database);
        ArrayList<String> tablenames = null;
        try {
            tablenames = model.getTables();
        } catch (QueryFailedException e) {
            // todo set error
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
