package controllers;

import exception.DatabaseNotFoundException;
import exception.QueryFailedException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import models.schema.Column;
import models.schema.Row;
import models.schema.Table;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Class TableController
 */
public class TableController extends AppController implements Initializable {

    private boolean edited = false;

    @FXML
    private Text tablename;

    @FXML
    private TableView tableView;

    /**
     * Discard all changes and leave to Database GUI
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
     * Save (commit) all changes Record
     * TODO implement
     */
    @FXML
    public void saveChanges() {
    }

    /**
     * Delete single Record
     * TODO implement
     */
    @FXML
    public void deleteRecord() {
    }

    /**
     * Initialization hook
     * @param location URL
     * @param resources ResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get table reference. The database table that should be loaded
        Table tableReference = this.getTable();
        this.tablename.setText(tableReference.getName());

        try {
            // Make list of columns (cast the ArrayList<String> to List<String>)
            List<String> columns = tableReference.getAttributeNames();

            // iterate through all possible columns, generate them dynamically
            for (int i = 0; i < columns.size(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(columns.get(i));

                // register Callback to insert values.
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        List<String> rowValues = param.getValue();
                        String cellValue;
                        // When a value does not exist (array size to small), set an empty value
                        // TODO this may cause some bugs because of the key (column name) => value may differ
                        if (j < rowValues.size()) {
                            cellValue = rowValues.get(j);
                        } else {
                            cellValue = "";
                        }
                        return new SimpleStringProperty(cellValue);
                    }
                });
                // add all columns to the table
                this.tableView.getColumns().addAll(col);
            }

            // get all data by row
            ArrayList<Row> rows = tableReference.getRows();

            // iterate through the rows
            for (int i = 0; i < rows.size(); i++) {
                // Create a List that is readable by JavaFX
                ObservableList<String> row = FXCollections.observableArrayList();
                // Add all data of a row to the table readable List
                row.addAll(rows.get(i).getValues());
                // Add the row data to the table
                this.tableView.getItems().add(row);
            }
        } catch (QueryFailedException e) {
            e.printStackTrace();
        }
    }
}
