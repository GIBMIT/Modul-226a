package models.tables;

import exception.QueryFailedException;
import models.schema.Table;
import services.Container;

import java.sql.SQLException;

/**
 * ExecTable to execute queries
 */
public class ExecTable extends AbstractTable {
    public Table table;

    /**
     * ExecTable constructor
     */
    public ExecTable() {
        super(((Table) Container.getInstance().get("table")).getDatabase());
        this.table = (Table) Container.getInstance().get("table");
    }

    /**
     * Execute insert query
     * @param query String to execute
     * @return boolean true if successful
     */
    public boolean insert(String query) {
        return exec(query);
    }

    /**
     * Execute update query
     * @param query String to execute
     * @return boolean true if successful
     */
    public boolean update(String query) {
        return exec(query);
    }

    /**
     * Execute a query and catch it's exceptions
     * @param query String
     * @return boolean true if execution was sucessful
     */
    private boolean exec(String query) {

        try {
            this.execute(query);
        } catch (QueryFailedException e) {
            try {
                this.closeStatement();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
