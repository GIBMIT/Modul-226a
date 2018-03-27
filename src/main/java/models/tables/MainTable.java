package models.tables;

import exception.QueryFailedException;
import models.schema.Database;
import models.schema.Row;
import models.schema.Table;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainTable extends AbstractTable {

    public MainTable(Database db) {
        super(db);
    }

    public ArrayList<String> getTables() throws QueryFailedException {
        ArrayList<String> result = new ArrayList<String>();
        try {
            DatabaseMetaData md = database.getConnection().getMetaData();
            ResultSet tables = md.getTables(null, null, "%", null);
            while (tables.next()) {
                result.add(tables.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<Row> getTableValues(Table table) throws QueryFailedException {
        String query = String.format("SELECT * FROM %s", table.getName());
        try {
            ResultSet rs = this.execute(query);
            int size = table.getAttributeNames().size();
            ArrayList<Row> result = new ArrayList<>();
            while(rs.next()) {
                Row row = new Row();
                for (int i = 1; i < size; i++)  {
                    String value = rs.getString(i);
                    row.addValue(value);
                }
                result.add(row);
            }
            this.closeStatement();
            return result;
        } catch (QueryFailedException | SQLException e) {
            e.printStackTrace();
            throw new QueryFailedException(String.format("Could not select values from %s", table.getName()));
        }
    }
}
