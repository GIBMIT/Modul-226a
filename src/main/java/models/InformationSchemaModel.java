package models;

import com.sun.deploy.ref.AppModel;
import exception.QueryFailedException;
import services.Attribute;
import services.Database;
import services.Row;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class InformationSchemaModel extends AbstractModel {

    public InformationSchemaModel(Database db) {
        super(db);
    }

    public ArrayList<Attribute> getTableAttributes(String table) throws QueryFailedException {
        String query = String.format("SELECT " +
                "COLUMN_NAME," +
                "DATA_TYPE," +
                "CHARACTER_MAXIMUM_LENGTH," +
                "IS_NULLABLE," +
                "COLUMN_KEY," +
                "COLUMN_DEFAULT," +
                "EXTRA," +
                "COLUMN_COMMENT " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_NAME = '%s';", table);
        try {
            ResultSet rs = this.execute(query);
            ArrayList<Attribute> result = new ArrayList<>();
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("DATA_TYPE");
                int length = rs.getInt("CHARACTER_MAXIMUM_LENGTH");
                boolean isNullable = rs.getString("IS_NULLABLE").equals("YES");
                boolean isPrimary = rs.getString("COLUMN_KEY").equals("PRI");
                boolean isAutoIncrement = rs.getString("EXTRA").toUpperCase().contains("AUTO_INCREMENT");
                String defaultValue = rs.getString("COLUMN_DEFAULT");
                String extra = rs.getString("EXTRA");
                String comment = rs.getString("COLUMN_COMMENT");
                Attribute attribute = new Attribute(columnName, columnType, length, isNullable, isPrimary, isAutoIncrement, defaultValue, extra, comment);
                result.add(attribute);
            }
            this.closeStatement();
            return result;
        } catch (QueryFailedException | SQLException e) {
            e.printStackTrace();
            throw new QueryFailedException("Getting table attributes failed");
        }
    }

    public ArrayList<String> getAttributeNames(String table) throws QueryFailedException {
        String query = String.format("SELECT COLUMN_NAME FROM information_schema.Columns WHERE TABLE_NAME = '%s' AND TABLE_SCHEMA = '%s'", table, database.getDatabase());
        try {
            ResultSet rs = this.execute(query);
            ArrayList<String> attributeNames = new ArrayList<String>();
            while(rs.next()) {
                attributeNames.add(rs.getString("COLUMN_NAME"));
            }
            return attributeNames;
        } catch (QueryFailedException | SQLException e) {
            e.printStackTrace();
            throw new QueryFailedException("Failed getting attribute names");
        }
    }
}
