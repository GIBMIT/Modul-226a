package controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import exception.DatabaseNotFoundException;
import exception.QueryFailedException;
import exception.TableNotFoundException;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import models.schema.AttributeRow;
import models.schema.Column;
import models.schema.Table;
import models.tables.CRUDTable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class EditController extends AppController implements Initializable {
    @FXML
    private Text tablenameText;

    @FXML
    private TextField tablename;

    @FXML
    private TableView tableView;

    @FXML
    private TableColumn<AttributeRow, String> attributeNameCol;
    @FXML
    private TableColumn<AttributeRow, String> attributeTypeCol;
    @FXML
    private TableColumn<AttributeRow, Integer> attributeLengthCol;
    @FXML
    private TableColumn<AttributeRow, Boolean> attributeIsNullableCol;
    @FXML
    private TableColumn<AttributeRow, Boolean> attributeIsPrimaryKeyCol;
    @FXML
    private TableColumn<AttributeRow, Boolean> attributeIsAutoIncrementCol;
    @FXML
    private TableColumn<AttributeRow, String> attributeDefaultCol;
    @FXML
    private TableColumn<AttributeRow, String> attributeCommentCol;

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
        setCellValueFactories();

        Callback<TableColumn<AttributeRow, String>, TableCell<AttributeRow, String>> stringCellFactory = p -> new StringEditCell();
        Callback<TableColumn<AttributeRow, Boolean>, TableCell<AttributeRow, Boolean>> booleanCellFactory = p -> new BooleanEditCell();
        Callback<TableColumn<AttributeRow, Integer>, TableCell<AttributeRow, Integer>> integerCellFactory = p -> new IntegerEditCell();
        this.attributeNameCol.setCellFactory(stringCellFactory);
        this.attributeTypeCol.setCellFactory(stringCellFactory);
        this.attributeLengthCol.setCellFactory(integerCellFactory);
        this.attributeIsNullableCol.setCellFactory(booleanCellFactory);
        this.attributeIsPrimaryKeyCol.setCellFactory(booleanCellFactory);
        this.attributeIsAutoIncrementCol.setCellFactory(booleanCellFactory);
        this.attributeDefaultCol.setCellFactory(stringCellFactory);
        this.attributeCommentCol.setCellFactory(stringCellFactory);


        if (!tableName.equalsIgnoreCase("new Table")) {
            this.tablename.setText(tableReference.getName());
            try {
                tableReference.getColumns().forEach((Column col) -> {
                    AttributeRow row = new AttributeRow(
                            col.getName(),
                            col.getType(),
                            col.getLength(),
                            col.isNullable(),
                            col.isPrimary(),
                            col.isAutoIncrement(),
                            col.getDefaultValue(),
                            col.getComment()
                    );
                    this.tableView.getItems().add(row);
                });
            } catch (QueryFailedException e) {
                e.printStackTrace();
            }
        }

        this.createNullableRow();
    }

    private void setCellValueFactories() {
        // iterate through all possible columns, generate them dynamically
        this.attributeNameCol.setCellValueFactory(cellData -> cellData.getValue().attributeNameProperty());
        this.attributeTypeCol.setCellValueFactory(cellData -> cellData.getValue().attributeTypeProperty());
        this.attributeLengthCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {

            @Override
            public String toString(Integer object) {
                try {
                    return object.toString();
                } catch (NullPointerException e) {
                    return "0";
                }
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NullPointerException e) {
                    return null;
                }
            }

        }));
        this.attributeIsNullableCol.setCellValueFactory(cellData -> cellData.getValue().isNullableProperty());
        this.attributeIsPrimaryKeyCol.setCellValueFactory(cellData -> cellData.getValue().isPrimaryKeyProperty());
        this.attributeIsAutoIncrementCol.setCellValueFactory(cellData -> cellData.getValue().isAutoIncrementProperty());
        this.attributeDefaultCol.setCellValueFactory(cellData -> cellData.getValue().attributeDefaultProperty());
        this.attributeCommentCol.setCellValueFactory(cellData -> cellData.getValue().attributeCommentProperty());
    }

    private void createNullableRow() {
        AttributeRow nullableRow = new AttributeRow(
                "NULL",
                "NULL",
                0,
                false,
                false,
                false,
                null,
                null
        );
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
    class StringEditCell extends TableCell<AttributeRow, String> {
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
//                        commitEdit();
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
    class BooleanEditCell extends TableCell<AttributeRow, Boolean> {
        private CheckBox checkBox;

        public BooleanEditCell() {
            checkBox = new CheckBox();
            checkBox.setDisable(true);
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (isEditing()) {
                    commitEdit(newValue);
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

        public void commitEdit(Boolean value) {
            super.commitEdit(value);
            checkBox.setDisable(false);
        }

        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (!isEmpty()) {
                boolean bool = item;
                checkBox.setSelected(bool);
            }
        }
    }

    class IntegerEditCell extends TableCell<AttributeRow, Integer> {
        private TextField textField;

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null && true) {
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
        public void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (empty && true) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing() && true) {
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
//                        commitEdit();
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
}
