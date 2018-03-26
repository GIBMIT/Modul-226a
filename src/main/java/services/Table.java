package services;

import exception.QueryFailedException;
import models.InformationSchemaModel;
import models.MainModel;

import java.util.ArrayList;

public class Table {
    private Database database;
    private String table;
    private ArrayList<Attribute> attributes;
    private ArrayList<Row> rows;
    private ArrayList<String> attributeNames;

    public Table(Database database, String table){
        this.database = database;
        this.table = table;
    }

    public ArrayList<Attribute> getAttributes() throws QueryFailedException {
        this.attributes = null;
        InformationSchemaModel m = new InformationSchemaModel(this.database);
        this.attributes = m.getTableAttributes(this.table);
        return this.attributes;
    }

    public ArrayList<Row> getRows() throws QueryFailedException {
        this.rows = null;
        MainModel m = new MainModel(this.database);
        this.rows = m.getTableValues(this);
        return this.rows;
    }

    public ArrayList<String> getAttributeNames() throws QueryFailedException {
        this.attributeNames = null;
        InformationSchemaModel i = new InformationSchemaModel(this.database);
        this.attributeNames = i.getAttributeNames(this.table);
        return this.attributeNames;
    }

    public String getName() {
        return this.table;
    }
}
