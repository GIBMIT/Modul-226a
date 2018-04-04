package models.schema;

import exception.QueryFailedException;
import models.tables.MainTable;
import models.tables.TableSchema;

import java.util.ArrayList;

public class Table {
    private Database database;
    private String table;
    private ArrayList<Column> columns;
    private ArrayList<Row> rows;
    private ArrayList<String> attributeNames;

    public Table(Database database, String table){
        this.database = database;
        this.table = table;
    }

    public Database getDatabase() {
        return this.database;
    }

    public ArrayList<Column> getColumns() throws QueryFailedException {
        this.columns = null;
        TableSchema m = new TableSchema(this.database);
        this.columns = m.getTableAttributes(this.table);
        return this.columns;
    }

    public ArrayList<Row> getRows() throws QueryFailedException {
        this.rows = null;
        MainTable m = new MainTable(this.database);
        this.rows = m.getTableValues(this);
        return this.rows;
    }

    public ArrayList<String> getAttributeNames() throws QueryFailedException {
        this.attributeNames = null;
        TableSchema i = new TableSchema(this.database);
        this.attributeNames = i.getAttributeNames(this.table);
        return this.attributeNames;
    }

    public String getName() {
        return this.table;
    }
}
