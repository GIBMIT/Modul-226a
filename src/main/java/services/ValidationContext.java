package services;

import java.util.ArrayList;
import java.util.HashMap;

public class ValidationContext {
    private String message = "Please check your data";
    private ArrayList<HashMap<String, String>> errors = new ArrayList<>();

    public void setError(String field, String message) {
        HashMap<String, String> error = new HashMap<>();
        error.put("field", field);
        error.put("message", message);
        this.errors.add(error);
    }

    public ArrayList<HashMap<String, String>> getErrors() {
        return errors;
    }

    public boolean fails() {
        return this.errors.size() > 0;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
