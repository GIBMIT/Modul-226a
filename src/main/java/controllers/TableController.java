package controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import exception.DatabaseNotFoundException;
import exception.QueryFailedException;
import exception.TableNotFoundException;
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
import models.tables.ExecTable;
import services.Container;
import services.ValidationContext;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class TableController
 */
public class TableController extends AppController implements Initializable {

    private boolean edited = false;

    @FXML
    private Text tablename;

    @FXML
    private TableView tableView;

    @FXML
    private Text error;

    private static StringBuilder updateQuery = new StringBuilder();
    private static StringBuilder insertQuery = new StringBuilder();
    private static HashMap<String, String> insertValues = new HashMap<>();

    /**
     * Save (commit) all changes Record
     */
    @FXML
    public void saveChanges() {
        ExecTable table = new ExecTable();
        boolean hasUpdated = true;
        if (updateQuery.length() > 0) {
            hasUpdated = table.update(updateQuery.toString());
            updateQuery.delete(0, updateQuery.length());
        }
        boolean hasInserted = true;
        if (insertValues.size() > 0) {
            ValidationContext validationContext = this.validate();
            if (validationContext.fails()) {
                StringBuilder message = new StringBuilder();
                validationContext.getErrors().forEach(map -> {
                    String field = map.get("field");
                    String msg = map.get("message");
                    message.append(String.format("Field: %s %s\n", field, msg));

                });

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("FAILURE");
                alert.setHeaderText("");
                alert.setContentText(message.toString());

                alert.show();

                return;
            }
            hasInserted = insert(insertValues);
        }

        if (hasUpdated && hasInserted) {
            try {
                // impossible to throw an error here
                this.initDatabaseGUI();
            } catch (IOException | DatabaseNotFoundException e) {
                e.printStackTrace();
                this.error.setText(e.getMessage());
            }
        } else {
            this.error.setText("Your input is invalid");
        }
    }

    /**
     * Insert values.
     *
     * @param values HashMap<String, String> with the rows and the matching value to insert
     * @return boolean True if inserted successfully
     */
    boolean insert(HashMap<String, String> values) {
        Table t = getTable();
        StringBuilder keys = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        values.forEach((String key, String val) -> {
            keys.append(String.format("%s, ", key));
            vals.append(String.format("'%s', ", val));
        });
        keys.delete(keys.length() - 2, keys.length());
        vals.delete(vals.length() - 2, vals.length());
        insertQuery.append(String.format("INSERT INTO %s (%s) VALUES (%s);", t.getName(), keys.toString(), vals.toString()));
        ExecTable table = new ExecTable();
        keys.delete(0, keys.length());
        vals.delete(0, vals.length());
        boolean hasInserted = table.insert(insertQuery.toString());
        insertQuery.delete(0, insertQuery.length());
        return hasInserted;
    }

    /**
     * Validate
     * @return ValidationContext
     */
    ValidationContext validate() {
        Table table = getTable();
        ValidationContext validationContext = new ValidationContext();
        try {
            table.getColumns().forEach((Column col) -> {
                if (!col.isNullable() && !(col.isAutoIncrement() && col.isPrimary())) {
                    String value = insertValues.get(col.getName().toLowerCase());
                    if (value == null || value.equalsIgnoreCase("NULL")) {
                        validationContext.setError(col.getName(), "Can not be null");
                    }
                }
            });
        } catch (QueryFailedException e) {
            e.printStackTrace();
            this.error.setText(e.getMessage());
        }
        return validationContext;
    }

    /**
     * Delete single Record
     */
    @FXML
    public void deleteRecord() {
        ObservableListWrapper row = (ObservableListWrapper) this.tableView.getSelectionModel().getSelectedItem();
        HashMap<String, String> selectors = new HashMap<>();
        try {
            AtomicInteger counter = new AtomicInteger();
            ArrayList<Column> columns = this.getTable().getColumns();
            columns.forEach((Column col) -> {
                if (col.isPrimary()) {
                    String name = col.getName();
                    String value = "NULL";
                    if (row.size() > counter.get()) {
                        value = (String) row.get(counter.get());
                    }

                    selectors.put(name, value);
                }
                counter.getAndIncrement();
            });

            StringBuilder query = new StringBuilder(String.format("DELETE FROM %s WHERE ", this.getTable().getName()));
            selectors.forEach((String rowName, String value) -> {
                if (value.equalsIgnoreCase("NULL")) {
                    query.append(String.format("%s IS NULL", rowName));
                } else {
                    query.append(String.format("%s = %s AND", rowName, value));
                }
            });
            query.delete(query.length() - 4, query.length());
            ExecTable table = new ExecTable();
            table.update(query.toString());
            query.delete(0, query.length());
            this.initTableGUI();
        } catch (QueryFailedException | TableNotFoundException | IOException e) {
            e.printStackTrace();
            this.error.setText(e.getMessage());
        }
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
            this.error.setText(e.getMessage());
        }

        // iterate through all possible columns, generate them dynamically
        for (int i = 0; i < columns.size(); i++) {
            TableColumn col = new TableColumn(columns.get(i));

            Callback<TableColumn, TableCell> cellFactory = p -> new EditingCell();

            // register Callback to insert values.
            setCellValueFactory(i, col);

            col.setCellFactory(cellFactory);

            // add all columns to the table
            this.tableView.getColumns().addAll(col);
        }
        populate(tableReference);
    }

    /**
     * Populate table fields with the table reference object
     *
     * @param tableReference
     */
    public void populate(Table tableReference) {
        // get all data by row
        ArrayList<Row> rows = null;
        try {
            rows = tableReference.getRows();
        } catch (QueryFailedException e) {
            this.error.setText(e.getMessage());
            e.printStackTrace();
            this.error.setText(e.getMessage());
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
        this.createNullableRow();
    }

    private void createNullableRow() {
        ObservableList<String> nullableRow = FXCollections.observableArrayList();
        try {
            this.getTable().getColumns().forEach(p -> {
                nullableRow.add("NULL");
            });
        } catch (QueryFailedException e) {
            e.printStackTrace();
            this.error.setText(e.getMessage());
            this.error.setText(e.getMessage());
        }
        this.tableView.getItems().add(nullableRow);
    }

    /**
     * Seen @ http://java-buddy.blogspot.ch/2013/03/javafx-editable-tableview-with-dynamic.html
     */
    class EditingCell extends TableCell<XYChart.Data, String> {
        private TextField textField;

        /**
         * Function to be called when starting to edit a cell
         */
        @Override
        public void startEdit() {
            // Get the container to get all columns from the table reference
            Container c = Container.getInstance();
            Table t = (Table) c.get("table");
            final ArrayList<String> primaryKeys = new ArrayList<>();

            try {
                ArrayList<Column> columns = t.getColumns();
                columns.forEach((Column column) -> {
                    if (column.isPrimary() && column.isAutoIncrement()) {
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

//            if (getTableRow().getIndex() + 1 >= getTableView().getItems().size()) {
//                createNullableRow();
//            }
        }

        /**
         * Method to call when editing a cell is cancelled.
         */
        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        /**
         * Update an item (fully copied from reference)
         *
         * @param item
         * @param empty
         */
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

        /**
         * Create the textfield to edit (fully copied from reference)
         */
        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

                /**
                 *  Handle incoming key event
                 * @param t
                 */
                @Override
                public void handle(KeyEvent t) {
                   if (t.getCode() == KeyCode.ENTER && textField.getText() != null) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }

                /**
                 * Save edited value.
                 * @param value
                 */
                void commitEdit(String value) {
                    int rowId = getTableRow().getIndex();

                    Object data = getTableView().getItems().get(rowId);
                    String field = getTableColumn().getText();

                    ArrayList<String> primaryKeys = getPrimaryKeys();

                    int tableRowCount = getTableView().getItems().size();
                    // check if selected row is the last one
                    if (tableRowCount - 2 <= rowId) {
                        //                        Object keys = getTableView().getItems().get(getTableRow().getIndex());
                        String key = getTableColumn().getText();
                        addForInsert(key, value);
                    } else {
                        update(value);
                    }

                    EditingCell.super.commitEdit(value);
                }

                /**
                 * Add value to insert queue (if type of action is an INSERT)
                 * @param key
                 * @param value
                 */
                void addForInsert(String key, String value) {
                    insertValues.put(key.toLowerCase(), value);
                }

                /**
                 * Create and execute update query for database.
                 * @param value
                 */
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

                /**
                 * Get all primary keys of the table.
                 * @return ArrayList<String> a list of the primary keys.
                 */
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
                        TableController.this.error.setText(e.getMessage());
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

        /**
         * Get Item as string
         *
         * @return String
         */
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
}
