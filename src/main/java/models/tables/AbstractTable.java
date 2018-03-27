package models.tables;

import exception.QueryFailedException;
import models.schema.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

abstract public class AbstractTable {
    protected static Database database;
    private Statement statement;

    AbstractTable(Database db) {
        database = db;
    }

    protected ResultSet execute(String query) throws QueryFailedException {
        this.statement = null;
        try {
            this.statement = database.getConnection().createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                this.closeStatement();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new QueryFailedException(String.format("Executing Query %s failed", query));
        }
    }

    protected final void closeStatement() throws SQLException {
        this.statement.close();
    }
}
