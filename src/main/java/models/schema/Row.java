package models.schema;

import java.util.ArrayList;

/**
 * Dynamic object to store the values for each row in the table in tableview.fxml
 */
public class Row {
    private ArrayList<String> values = new ArrayList<>();

    /**
     * Add an value to the row
     * @param value String
     */
    public void addValue(String value) {
        this.values.add(value);
    }

    /**
     * Get all values
     * @return ArrayList
     */
    public ArrayList<String> getValues(){
        return this.values;
    }
}
