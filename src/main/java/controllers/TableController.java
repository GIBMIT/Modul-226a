package controllers;

import exception.DatabaseNotFoundException;
import exception.QueryFailedException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import services.Row;
import services.Table;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class TableController extends AppController implements Initializable {

    @FXML
    private Text tablename;

    @FXML
    private TableView tableView;

    @FXML
    public void discardChanges() {
        try {
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

    @FXML
    public void saveChanges() {
    }

    @FXML
    public void deleteRecord() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Table tableReference = this.getTable();
        this.tablename.setText(tableReference.getName());
        ArrayList<Row> rows = null;
        try {
            rows = tableReference.getRows();
            int rowCount = rows.size();
            ArrayList<String> attributeNames = tableReference.getAttributeNames();
            int attributeCount = attributeNames.size();

            for (int i = 0; i < attributeCount; i++) {
                TableColumn col = new TableColumn(attributeNames.get(i));
                this.tableView.getColumns().add(col);
            }

            this.tableView.getItems().clear();
            this.tableView.setEditable(true);
            for (int i = 0; i < rowCount; i++) {
                ArrayList<String> row = rows.get(i).getValues();
                for (int x = 0; x < row.size(); x++) {
                    String value = row.get(x);
                    this.tableView.getItems().add(value);
                }
            }
        } catch (QueryFailedException e) {
            e.printStackTrace();
        }
    }
}
