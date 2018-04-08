package models.tables;

import exception.QueryFailedException;
import models.schema.Database;
import services.Container;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abstract class to execute queries
 */
abstract public class AbstractTable {
    protected static Database database;
    private Statement statement;

    /**
     * AbstractTable constructor
     * @param db Database object
     */
    AbstractTable(Database db) {
        Container.getInstance().set("database", db);
        database = db;
    }

    /**
     * Execute a query and get the corresponding result set
     * @param query String
     * @return ResultSet
     * @throws QueryFailedException if anything failed
     */
    protected ResultSet execute(String query) throws QueryFailedException {
        this.statement = null;
        String type = query.substring(0, 6);
        try {
            this.statement = database.getConnection().createStatement();
            System.out.println(String.format("Executing:\n%s", query));
            if (type.equalsIgnoreCase("SELECT")) {
                return statement.executeQuery(query);
            } else {
                if (query.contains(";")) {
                    String[] queries = query.split(";");
                    for (int i = 0; i < queries.length; i++) {
                        query = queries[i] + ";";
                        statement.executeUpdate("SET foreign_key_checks = 0;");
                        statement.executeUpdate(query);
                        statement.executeUpdate("SET foreign_key_checks = 1;");
                    }
                } else {
                    statement.executeUpdate(query);
                }
                return null;
            }
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

    /**
     * Closes the statement after using it
     * This method should be used after any execute statement and the processing of the result set
     * @throws SQLException if closing the statement fails
     */
    protected final void closeStatement() throws SQLException {
        this.statement.close();
    }
}
