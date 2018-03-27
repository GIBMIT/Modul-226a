package models.schema;

import java.util.ArrayList;

public class Row {
    private ArrayList<String> values = new ArrayList<>();

    public void addValue(String value) {
        this.values.add(value);
    }

    public ArrayList<String> getValues(){
        return this.values;
    }
}
