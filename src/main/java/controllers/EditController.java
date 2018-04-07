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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import models.schema.Column;
import models.schema.Table;
import models.tables.CRUDTable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class EditController extends AppController implements Initializable {
    @FXML
    private Text tablenameText;

    @FXML
    private TextField tablename;

    @FXML
    private TableView tableView;

    private ArrayList<String> oldNames = new ArrayList<>();

    @FXML
    public void saveChanges() {
        alterTable();
    }

    @FXML
    public void deleteRecord() {
        ObservableListWrapper selection = (ObservableListWrapper) this.tableView.getSelectionModel().getSelectedItem();
        String column = selection.get(0).toString();
        String query = String.format("ALTER TABLE %s DROP COLUMN %s;", this.getTable().getName(), column);
        CRUDTable table = new CRUDTable();
        boolean hasUpdated = table.update(query);
        if (hasUpdated) {
            this.saveChanges();
            try {
                this.initEditGUI();
            } catch (TableNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Table tableReference = this.getTable();
        String tableName = tableReference.getName();
        this.tablenameText.setText(tableName);
        this.tableView.setEditable(true);

        List<String> columns = new ArrayList<>();
        columns.add("Name");
        columns.add("Type");
        columns.add("Length");
        columns.add("NN");
        columns.add("PK");
        columns.add("AI");
        columns.add("Default");
        columns.add("Comment");

        // iterate through all possible columns, generate them dynamically
        for (int i = 0; i < columns.size(); i++) {
            TableColumn col = new TableColumn(columns.get(i));

            Callback<TableColumn, TableCell> cellFactory = p -> new EditingCell();
            if (col.getText().length() == 2) {
                cellFactory = p -> new BooleanCell();
            }

            // register Callback to insert values.
            setCellValueFactory(i, col);

            col.setCellFactory(cellFactory);

            // add all columns to the table
            ObservableList<TableColumn> cols = this.tableView.getColumns();
            cols.addAll(col);
        }

        if (!tableName.equalsIgnoreCase("new Table")) {
            this.tablename.setText(tableReference.getName());
            try {
                ArrayList<Column> tableColumns = tableReference.getColumns();
                tableColumns.forEach((Column column) -> {
                    ObservableList values = FXCollections.observableArrayList();
                    values.add(column.getName());
                    values.add(column.getType());
                    values.add(column.getLength());
                    values.add(column.isNullable());
                    values.add(column.isPrimary());
                    values.add(column.isAutoIncrement());
                    values.add(column.getDefaultValue());
                    values.add(column.getComment());
                    this.tableView.getItems().add(values);
                });
            } catch (QueryFailedException e) {
                e.printStackTrace();
            }
        }

        this.oldNames.clear();
        for (int i = 0; i < this.tableView.getItems().size(); i++) {
            String name = (String) ((ObservableListWrapper) this.tableView.getItems().get(i)).get(0);
            this.oldNames.add(name);
        }

        this.createNullableRow();
    }

    private void createNullableRow() {
        ObservableList<String> nullableRow = FXCollections.observableArrayList();
        try {
            this.getTable().getColumns().forEach(p -> nullableRow.add("NULL"));
        } catch (QueryFailedException e) {
            e.printStackTrace();
        }
        this.tableView.getItems().add(nullableRow);
    }

    /**
     * Alter the table
     */
    private void alterTable() {
        StringBuilder alterQuery = getAlterQuery();
        CRUDTable table = new CRUDTable();
        boolean hasExecutedAlter = table.update(alterQuery.toString());

        StringBuilder createQuery = getCreateQueryForExistingTable();
        boolean hasExecutedCreate = table.update(createQuery.toString());
        if (hasExecutedAlter && hasExecutedCreate) {
            StringBuilder primaryKeyQuery = getPrimaryKeyQuery();
            hasExecutedAlter = table.update(primaryKeyQuery.toString());
            if (hasExecutedAlter) {
                try {
                    this.initDatabaseGUI();
                } catch (IOException | DatabaseNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Something went wrong. Sorry");
        }
    }

    /**
     * Get Query to set the primary keys.
     *
     * @return StringBuilder
     */
    private StringBuilder getPrimaryKeyQuery() {
        StringBuilder query2 = new StringBuilder(String.format("ALTER TABLE %s DROP PRIMARY KEY, ADD PRIMARY KEY(", this.getTable().getName()));
        this.tableView.getItems().forEach(el -> {
            ObservableListWrapper element = (ObservableListWrapper) el;
            if (this.canUse(element)) {
                if ((boolean) element.get(4)) {
                    query2.append(((String) element.get(0)) + ",");
                }
            }
        });
        query2.delete(query2.length() - 1, query2.length());
        query2.append(");");
        return query2;
    }

    private StringBuilder getCreateQueryForExistingTable() {
        ObservableList items = this.tableView.getItems();
        int index = oldNames.size();
        int itemSize = items.size();
        StringBuilder query = new StringBuilder(String.format("ALTER TABLE %s ", this.getTable().getName()));
        if (itemSize >= index) {
            for (; index < itemSize; index++) {
                ObservableListWrapper item = (ObservableListWrapper) items.get(index);
                query.append(String.format("ADD COLUMN %s ", item.get(0)));
                getAlterQueryForColumn(query, item);
            }
            query.append(";");
        }
        return query;
    }

    /**
     * Get query to alter the table.
     *
     * @return StringBuilder query
     */
    private StringBuilder getAlterQuery() {
        AtomicInteger counter = new AtomicInteger(0);
        StringBuilder query = new StringBuilder(String.format("ALTER TABLE %s ", this.getTable().getName(), this.getTable().getName()));

        this.tableView.getItems().forEach(el -> {
            ObservableListWrapper element = (ObservableListWrapper) el;
            if (this.canUse(element)) {
                query.append(String.format("CHANGE COLUMN %s %s ", oldNames.get(counter.get()), element.get(0)));
                getAlterQueryForColumn(query, element);

                counter.getAndIncrement();
            }
        });

        query.delete(query.length() - 1, query.length());
        query.append(";");
        return query;
    }

    private void getAlterQueryForColumn(StringBuilder query, ObservableListWrapper element) {
        String type = (String) element.get(1);
        String length = Integer.toString((int) element.get(2));
        query.append(String.format("%s(%s) ", type, length));
        if ((boolean) element.get(3)) {
            query.append("NOT NULL ");
        }

        if ((boolean) element.get(5)) {
            query.append("AUTO_INCREMENT ");
        }

        if (element.get(6) != null && !element.get(6).toString().equalsIgnoreCase("NULL")) {
            query.append(String.format("DEFAULT '%s' ", element.get(6).toString()));
        }

        if (element.get(7) != null && !element.get(7).toString().equalsIgnoreCase("NULL")) {
            query.append(String.format("COMMENT '%s' ", ((String) element.get(7)).toString()));
        }

        if ((boolean) element.get(5)) {
            query.append("AUTO_INCREMENT");
        }

        query.append(",");
    }

    /**
     * Check if element can be used for alter query
     *
     * @param element ObservableListWrapper
     * @return boolean true if element can be used
     */
    private boolean canUse(ObservableListWrapper element) {
        return !(element.get(0) == null && element.get(1) == null) && (!((String) element.get(0)).equalsIgnoreCase("NULL") && !((String) element.get(1)).equalsIgnoreCase("NULL"));
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
            super.startEdit();

            if (textField == null) {
                createTextField();
            }

            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();

            if (getTableRow().getIndex() + 1 >= getTableView().getItems().size()) {
                createNullableRow();
            }
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

    /**
     * Seen @ https://stackoverflow.com/questions/7217625/how-to-add-checkboxs-to-a-tableview-in-javafx
     */
    class BooleanCell extends TableCell<XYChart.Data, String> {
        private CheckBox checkBox;

        public BooleanCell() {
            checkBox = new CheckBox();
            checkBox.setDisable(true);
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (isEditing()) {
                    commitEdit(newValue.toString().equalsIgnoreCase("true") ? "true" : "false");
                }
            });
            this.setGraphic(checkBox);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            this.setEditable(true);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (isEmpty()) {
                return;
            }
            checkBox.setDisable(false);
            checkBox.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            checkBox.setDisable(true);
        }

        public void commitEdit(String value) {
            super.commitEdit(value);
            checkBox.setDisable(false);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (!isEmpty()) {
                boolean bool = item.equalsIgnoreCase("true");
                checkBox.setSelected(bool);
            }
        }
    }
}
