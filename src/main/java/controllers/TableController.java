package controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import exception.DatabaseNotFoundException;
import exception.QueryFailedException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import models.schema.Column;
import models.schema.Row;
import models.schema.Table;
import models.tables.CRUDTable;
import services.Container;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Class TableController
 */
public class TableController extends AppController implements Initializable {

    private boolean edited = false;

    @FXML
    private Text tablename;

    @FXML
    private TableView tableView;

    private static StringBuilder updateQuery = new StringBuilder();

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
        CRUDTable table = new CRUDTable();
        boolean hasUpdated = table.update(updateQuery.toString());
        if (hasUpdated) {
            try {
                // impossible to throw an error here
                updateQuery.delete(0, updateQuery.length());
                this.initDatabaseGUI();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DatabaseNotFoundException e) {
                e.printStackTrace();
            }
        }
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
     *
     * @param location  URL
     * @param resources ResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get table reference. The database table that should be loaded
        Table tableReference = this.getTable();
        this.tablename.setText(tableReference.getName());
        this.tableView.setEditable(true);

        // Make list of columns (cast the ArrayList<String> to List<String>)
        List<String> columns = null;
        try {
            columns = tableReference.getAttributeNames();
        } catch (QueryFailedException e) {
            e.printStackTrace();
        }

        // iterate through all possible columns, generate them dynamically
        for (int i = 0; i < columns.size(); i++) {
            final int j = i;
            TableColumn col = new TableColumn(columns.get(i));

            Callback<TableColumn, TableCell> cellFactory =
                    new Callback<TableColumn, TableCell>() {

                        @Override
                        public TableCell call(TableColumn p) {
                            return new EditingCell();
                        }
                    };

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
                        cellValue = "NULL";
                    }
                    return new SimpleStringProperty(cellValue);
                }
            });

            col.setCellFactory(cellFactory);

            // add all columns to the table
            this.tableView.getColumns().addAll(col);
        }
        populate(tableReference);
    }

    public void populate(Table tableReference) {
        // get all data by row
        ArrayList<Row> rows = null;
        try {
            rows = tableReference.getRows();
        } catch (QueryFailedException e) {
            e.printStackTrace();
        }

        // iterate through the rows
        for (int i = 0; i < rows.size(); i++) {
            // Create a List that is readable by JavaFX
            ObservableList<String> row = FXCollections.observableArrayList();
            // Add all data of a row to the table readable List
            row.addAll(rows.get(i).getValues());
            // Add the row data to the table
            this.tableView.getItems().add(row);
        }

        int colSize = this.tableView.getColumns().size();
        ObservableList<String> r = FXCollections.observableArrayList();
        ArrayList<String> l = new ArrayList<>();
        for (int i = 0; i < colSize; i++) {
            l.add("NULL");
        }
        r.addAll(l);
        this.tableView.getItems().add(r);
    }

    /**
     * From http://java-buddy.blogspot.ch/2013/03/javafx-editable-tableview-with-dynamic.html
     */
    class EditingCell extends TableCell<XYChart.Data, String> {
        private TextField textField;

        private ArrayList<String> primarykeys = new ArrayList<>();

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            // Get the container to get all columns from the table reference
            Container c = Container.getInstance();
            Table t = (Table) c.get("table");
            final ArrayList<String> primaryKeys = new ArrayList<>();

            try {
                ArrayList<Column> columns = t.getColumns();
                columns.forEach((Column column) -> {
                    if (column.isPrimary()) {
                        primaryKeys.add(column.getName());
                    }
                });
            } catch (QueryFailedException e) {
                e.printStackTrace();
            }

            String k = getTableColumn().getText().toString();

            if (primaryKeys.contains(k)) {
                return;
            }

            super.startEdit();

            if (textField == null) {
                createTextField();
            }

            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getString());
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

                HashMap<String, String> values = new HashMap<>();

                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER && textField.getText() != null) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }

                void commitEdit(String value) {
                    int rowId = getTableRow().getIndex();

                    Object data = getTableView().getItems().get(rowId);
                    String field = getTableColumn().getText();

                    ArrayList<String> primaryKeys = getPrimaryKeys();

                    int tableRowCount = getTableView().getItems().size();
                    // check if selected row is the last one
                    if (tableRowCount > getTableRow().getIndex()) {
                        String key = getTableView().getItems().get(getTableRow().getIndex()).toString();
                        addForInsert(key, value);
                        try {
                            int colCount = getTable().getColumns().size();
                            if (values.size() >= colCount) {
                                insert(values);
                            }
                        } catch (QueryFailedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        update(value);
                    }

                    EditingCell.super.commitEdit(value);
                }

                void addForInsert(String key, String value) {
                    values.put(key, value);
                }

                void insert(HashMap<String, String> values) {
                    Table t = getTable();
                    StringBuilder insertQuery = new StringBuilder();
                    StringBuilder keys = new StringBuilder();
                    StringBuilder vals = new StringBuilder();
                    values.forEach((String key, String val) -> {
                        keys.append(key + ", ");
                        vals.append(val + ", ");
                    });
                    keys.delete(keys.length() - 2, keys.length());
                    vals.delete(vals.length() - 2, vals.length());
                    insertQuery.append(String.format("INSERT INTO %s(%s) VALUES(%s);", t.getName(), keys.toString(), vals.toString()));
                    CRUDTable table = new CRUDTable();
                    keys.delete(0, keys.length());
                    vals.delete(0, vals.length());
                    table.insert(insertQuery.toString());
                    insertQuery.delete(0, insertQuery.length());
                    // TODO continue with insert here
                }

                void update(String value) {
                    HashMap<String, String> where = new HashMap<>();
                    int rowId = getTableRow().getIndex();
                    Object data = getTableView().getItems().get(rowId);
                    String field = getTableColumn().getText();

                    int size = ((ObservableListWrapper) data).size();
                    Object columns = getTableView().getColumns();
                    for (int i = 0; i < size; i++) {
                        String k = ((TableColumn) ((ObservableListWrapper) columns).get(i)).getText().toString();

                        ArrayList<String> primaryKeys = getPrimaryKeys();

                        // Check if key is a primary key
                        if (!primaryKeys.contains(k)) {
                            // Continue if not
                            continue;
                        }

                        // Get the value
                        Object b = ((ObservableListWrapper) data).get(i);

                        String v = null;
                        // check if any value exists for the value (b, the value v)
                        if (b != null) {
                            v = b.toString();
                        }
                        where.put(k, v);
                    }
                    String main = String.format("UPDATE %s SET %s = '%s' WHERE ", ((Table) Container.getInstance().get("table")).getName(), field, value);
                    updateQuery.append(main);
                    where.forEach((String key, String val) -> {
                        if (val != null) {
                            updateQuery.append(String.format("%s = '%s' AND ", key, val));
                        }
                    });

                    updateQuery.delete(updateQuery.length() - 4, updateQuery.length());
                    updateQuery.append(";");
                }

                ArrayList<String> getPrimaryKeys() {
                    final ArrayList<String> primaryKeys = new ArrayList<>();
                    try {
                        getTable().getColumns().forEach((Column column) -> {
                            if (column.isPrimary()) {
                                primaryKeys.add(column.getName());
                            }
                        });
                    } catch (QueryFailedException e) {
                        e.printStackTrace();
                    }
                    return primaryKeys;
                }

                Table getTable() {
                    // Get the container to get all columns from the table reference
                    Container c = Container.getInstance();
                    return (Table) c.get("table");
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
}
