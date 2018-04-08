package models.schema;

import exception.QueryFailedException;
import models.tables.MainTable;
import models.tables.TableSchema;

import java.util.ArrayList;

/**
 * Table class to store all data of the table
 */
public class Table {
    private boolean isCreated;
    private Database database;
    private String table;
    private ArrayList<Column> columns;
    private ArrayList<Row> rows;
    private ArrayList<String> attributeNames;

    /**
     * Table constructor
     * @param database Database the databse reference where the table is stored
     * @param table String the table name
     */
    public Table(Database database, String table){
        this.database = database;
        this.table = table;
        this.isCreated = true;
    }

    /**
     * Set a new table name if it was changed
     * @param name String
     */
    public void setName(String name) {
        this.table = name;
    }

    /**
     * Mark the table as created (or not)
     * @param isCreated boolean true if table exists in database, used to create a new table
     */
    public void setIsCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    /**
     * Check if table is created
     * @return boolean
     */
    public boolean isCreated() {
        return isCreated;
    }

    /**
     * Get the referenced Database object
     * @return Database
     */
    public Database getDatabase() {
        return this.database;
    }

    /**
     * Get an ArrayList of all columns in the table
     * @return ArrayList
     * @throws QueryFailedException if getting columns failed
     */
    public ArrayList<Column> getColumns() throws QueryFailedException {
        this.columns = null;
        TableSchema m = new TableSchema(this.database);
        this.columns = m.getTableAttributes(this.table);
        return this.columns;
    }

    /**
     * Get all rows of the table
     * @return ArrayList
     * @throws QueryFailedException if getting rows faild
     */
    public ArrayList<Row> getRows() throws QueryFailedException {
        this.rows = null;
        MainTable m = new MainTable(this.database);
        this.rows = m.getTableValues(this);
        return this.rows;
    }

    /**
     * Get all attribute names
     * @return ArrayList
     * @throws QueryFailedException if getting attribute names failed
     */
    public ArrayList<String> getAttributeNames() throws QueryFailedException {
        this.attributeNames = null;
        TableSchema i = new TableSchema(this.database);
        this.attributeNames = i.getAttributeNames(this.table);
        return this.attributeNames;
    }

    /**
     * Get the table name
     * @return String
     */
    public String getName() {
        return this.table;
    }
}
