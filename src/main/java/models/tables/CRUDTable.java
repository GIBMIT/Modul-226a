package models.tables;

import exception.QueryFailedException;
import models.schema.Table;
import services.Container;

import java.sql.SQLException;

public class CRUDTable extends AbstractTable {
    public Table table;

    public CRUDTable() {
        super(((Table) Container.getInstance().get("table")).getDatabase());
        this.table = (Table) Container.getInstance().get("table");
    }

    public boolean insert(String query) {
        return exec(query);
    }

    public boolean update(String query) {
        return exec(query);
    }
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
