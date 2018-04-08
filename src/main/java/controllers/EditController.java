package controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import exception.DatabaseNotFoundException;
import exception.QueryFailedException;
import exception.TableNotFoundException;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import models.schema.AttributeRow;
import models.schema.Column;
import models.schema.Database;
import models.schema.Table;
import models.tables.ExecTable;
import services.Container;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class EditController extends AppController implements Initializable {
    @FXML
    private Text tablenameText;

    @FXML
    private Text error;

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

    @FXML
    public void saveChanges() {
        Table table = this.getTable();
        if (table.isCreated()) {
            alterTable();
        } else {
            boolean hasCreated = createTable();
            if (hasCreated) {
                try {
                    this.initDatabaseGUI();
                } catch (IOException | DatabaseNotFoundException e) {
                    this.error.setText("Something terrible happend. Please contact the developer. Code 666");
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void deleteRecord() {
        ObservableListWrapper selection = (ObservableListWrapper) this.tableView.getSelectionModel().getSelectedItem();
        String column = selection.get(0).toString();
        String query = String.format("ALTER TABLE %s DROP COLUMN %s;", this.getTable().getName(), column);
        ExecTable table = new ExecTable();
        boolean hasUpdated = table.update(query);
        if (hasUpdated) {
            this.saveChanges();
            try {
                this.initEditGUI();
            } catch (TableNotFoundException | IOException e) {
                this.error.setText(e.getMessage());
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
        setCellFactories();

        if (!tableName.equalsIgnoreCase("new Table")) {
            populate(tableReference);
        }

        this.createNullableRow();
    }

    private boolean createTable() {
        String name = this.tablename.getText();
        if (name.equalsIgnoreCase("") || name == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Tablename cannot be null");
            alert.show();
            return false;
        }
        StringBuilder query = new StringBuilder(String.format("CREATE TABLE %s (", name));
        this.tableView.getItems().forEach(el -> {
            AttributeRow row = (AttributeRow) el;
            if (this.canUse(row)) {
                query.append(row.getAttributeName()).append(" ");
                this.getColumnConfigurationSQL(query, row);
            }
        });
        query.deleteCharAt(query.length() - 1);
        query.append(");");
        Database db = (Database) Container.getInstance().get("database");
        boolean hasCreated = true;
        try {
            Statement stmt = db.getConnection().createStatement();
            System.out.println(String.format("Executing:\n%s", query.toString()));
            stmt.executeUpdate(query.toString());
        } catch (SQLException e) {
            hasCreated = false;
            this.error.setText(e.getMessage());
            e.printStackTrace();
        }

        if (hasCreated) {
            getTable().setName(name);
            getTable().setIsCreated(true);
        }


        try {
            Statement stmt = db.getConnection().createStatement();
            System.out.println(String.format("Executing:\n%s", query.toString()));
            StringBuilder primaryKeyQuery = this.getPrimaryKeyQuery();
            stmt.executeUpdate(primaryKeyQuery.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getTable().isCreated();
    }

    private void populate(Table tableReference) {
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
            this.error.setText(e.getMessage());
        }
    }

    /**
     * Set CellFactories (to edit cells)
     */
    private void setCellFactories() {
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
    }

    /**
     * Set CellValueFactories (to render cell data)
     */
    private void setCellValueFactories() {
        // iterate through all possible columns, generate them dynamically
        this.attributeNameCol.setCellValueFactory(cellData -> cellData.getValue().attributeNameProperty());
        this.attributeTypeCol.setCellValueFactory(cellData -> cellData.getValue().attributeTypeProperty());
        this.attributeLengthCol.setCellValueFactory(cellData -> cellData.getValue().attributeLengthProperty().asObject());
        this.attributeIsNullableCol.setCellValueFactory(cellData -> cellData.getValue().isNullableProperty().not());
        this.attributeIsPrimaryKeyCol.setCellValueFactory(cellData -> cellData.getValue().isPrimaryKeyProperty());
        this.attributeIsAutoIncrementCol.setCellValueFactory(cellData -> cellData.getValue().isAutoIncrementProperty());
        this.attributeDefaultCol.setCellValueFactory(cellData -> cellData.getValue().attributeDefaultProperty());
        this.attributeCommentCol.setCellValueFactory(cellData -> cellData.getValue().attributeCommentProperty());
    }

    /**
     * Create NULL row
     */
    private void createNullableRow() {
        AttributeRow nullableRow = new AttributeRow(
                "NULL",
                "NULL",
                0,
                true,
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
        ExecTable table = new ExecTable();
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
                    this.error.setText(e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Something went wrong. Are all values correct? (especially the Length. The incorrect use of Foreign Keys and NN causes Bugs, so Please don't edit those tables)");
            alert.show();
        }
    }

    /**
     * Get Query to set the primary keys.
     *
     * @return StringBuilder query
     */
    private StringBuilder getPrimaryKeyQuery() {
        StringBuilder query = new StringBuilder(String.format("ALTER TABLE %s DROP PRIMARY KEY, ADD PRIMARY KEY(", this.getTable().getName()));
        this.tableView.getItems().forEach(el -> {
            AttributeRow row = (AttributeRow) el;
            if (this.canUse(row)) {
                if (row.getIsPrimaryKey()) {
                    query.append(row.getAttributeName()).append(",");
                }
            }
        });
        query.deleteCharAt(query.length() - 1);
        query.append(");");
        query.append(String.format("ALTER TABLE %s ", this.getTable().getName()));
        this.tableView.getItems().forEach(el -> {
            AttributeRow row = (AttributeRow) el;
            if (this.canUse(row)) {
                if (row.getIsAutoIncrement()) {
                    query.append(String.format("MODIFY COLUMN %s %s(%s) AUTO_INCREMENT ", row.getAttributeName(), row.getAttributeType(), row.getAttributeLength()));
                    if (row.getIsNullable()) {
                        query.append("NOT NULL");
                    }
                    query.append(",");
                }
            }
        });
        query.deleteCharAt(query.length() - 1);
        query.append(";");
        return query;
    }

    /**
     * Get create row query for already existing table
     *
     * @return StringBuilder query
     */
    private StringBuilder getCreateQueryForExistingTable() {
        StringBuilder query = new StringBuilder(String.format("ALTER TABLE %s ", this.getTable().getName()));
        this.tableView.getItems().forEach(el -> {
            AttributeRow row = (AttributeRow) el;
            // Check if row is a new one (old name is NULL) and check if new Attribute name is not NULL
            if (row.getOldName().equalsIgnoreCase("NULL") && !row.getAttributeName().equalsIgnoreCase("NULL")) {
                query.append(String.format("ADD COLUMN %s ", row.getAttributeName()));
                getColumnConfigurationSQL(query, row);
            }
        });
        query.deleteCharAt(query.length() - 1);
        query.append(";");
        return query;
    }

    /**
     * Get query to alter the table.
     *
     * @return StringBuilder query
     */
    private StringBuilder getAlterQuery() {
        AtomicInteger counter = new AtomicInteger(0);
        StringBuilder query = new StringBuilder(String.format("ALTER TABLE %s ", this.getTable().getName()));
        String tablename = this.tablename.getText();
        query.append(String.format("RENAME %s, ", tablename));

        this.tableView.getItems().forEach(r -> {
            AttributeRow row = (AttributeRow) r;
            if (this.canUse(row)) {
                if (!row.getOldName().equalsIgnoreCase("NULL")) {
                    query.append(String.format("CHANGE COLUMN %s %s ", row.getOldName(), row.getAttributeName()));
                    getColumnConfigurationSQL(query, row);
                    counter.getAndIncrement();
                }
            }
        });

        query.delete(query.length() - 1, query.length());
        query.append(";");
        return query;
    }

    /**
     * Get alter query for single column.
     *
     * @param query
     * @param row
     */
    private void getColumnConfigurationSQL(StringBuilder query, AttributeRow row) {
        String type = row.getAttributeType();
        String length = Integer.toString(row.getAttributeLength());
        if (Integer.parseInt(length) <= 0) {
            length = "";
        }
        query.append(String.format("%s", type));
        if (length != null && !length.equals("")) {
            query.append(String.format("(%s)", length));
        }
        query.append(" ");
        if (row.getIsNullable()) {
            query.append("NOT NULL ");
        }

        if (row.getAttributeDefault() != null && !row.getAttributeDefault().equalsIgnoreCase("NULL")) {
            query.append(String.format("DEFAULT '%s' ", row.getAttributeDefault()));
        }

        if (row.getAttributeComment() != null && !row.getAttributeComment().equalsIgnoreCase("NULL") && !row.getAttributeComment().equalsIgnoreCase("")) {
            query.append(String.format("COMMENT '%s' ", row.getAttributeComment()));
        }

        query.append(",");
    }

    /**
     * Check if element can be used for alter query
     *
     * @param row ObservableListWrapper
     * @return boolean true if element can be used
     */
    private boolean canUse(AttributeRow row) {
        return !(row.getAttributeName() == null && row.getAttributeType() == null)
                && (!row.getAttributeName().equalsIgnoreCase("NULL") && !row.getAttributeType().equalsIgnoreCase("NULL"))
                && (!row.getAttributeName().equalsIgnoreCase("") && !row.getAttributeType().equalsIgnoreCase("")
        );
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
    class BooleanEditCell extends TableCell<AttributeRow, Boolean> {
        private CheckBox checkBox;

        /**
         * Constructor
         */
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

        /**
         * Start edit
         */
        @Override
        public void startEdit() {
            super.startEdit();
            if (isEmpty()) {
                return;
            }
            checkBox.setDisable(false);
            checkBox.requestFocus();
        }

        /**
         * Cancel edit
         */
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            checkBox.setDisable(true);
        }

        /**
         * Commit Edit and save changes
         * @param value Boolean
         */
        public void commitEdit(Boolean value) {
            super.commitEdit(value);
            checkBox.setDisable(false);
        }

        /**
         * Update item
         * @param item Boolean
         * @param empty boolean
         */
        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (!isEmpty()) {
                boolean bool = item;
                checkBox.setSelected(bool);
            }
        }
    }

    /**
     * Integer Cell Class
     */
    public class IntegerEditCell extends TableCell<AttributeRow, Integer> {

        private final TextField textField = new TextField();
        private final Pattern intPattern = Pattern.compile("-?\\d+");

        /**
         * Constructor
         */
        public IntegerEditCell() {
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    processEdit();
                }
            });
            textField.setOnAction(event -> processEdit());
        }

        /**
         * Process Edit
         */
        private void processEdit() {
            String text = textField.getText();
            if (intPattern.matcher(text).matches()) {
                commitEdit(Integer.parseInt(text));
            } else {
                cancelEdit();
            }
        }

        /**
         * Update item
         * @param value Integer
         * @param empty boolean
         */
        @Override
        public void updateItem(Integer value, boolean empty) {
            super.updateItem(value, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                setText(null);
                textField.setText(value.toString());
                setGraphic(textField);
            } else {
                setText(value.toString());
                setGraphic(null);
            }
        }

        /**
         * Start edit
         */
        @Override
        public void startEdit() {
            super.startEdit();
            Number value = getItem();
            if (value != null) {
                textField.setText(value.toString());
                setGraphic(textField);
                setText(null);
            }
        }

        /**
         * Cancel edit
         */
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().toString());
            setGraphic(null);
        }

        /**
         * Commit edit
         * @param value Integer
         */
        @Override
        public void commitEdit(Integer value) {
            super.commitEdit(value);
            ((AttributeRow) this.getTableRow().getItem()).setAttributeLength(value);
        }
    }
}
