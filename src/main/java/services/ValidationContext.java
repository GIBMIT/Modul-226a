package services;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to save the validation context of a situation
 */
public class ValidationContext {
    private String message = "Please check your data";
    private ArrayList<HashMap<String, String>> errors = new ArrayList<>();

    /**
     * Set an error for a field with a message
     * @param field String name of the field in the form
     * @param message String the message that should be saved with the error of the field validation
     */
    public void setError(String field, String message) {
        HashMap<String, String> error = new HashMap<>();
        error.put("field", field);
        error.put("message", message);
        this.errors.add(error);
    }

    /**
     * Get all errors
     * @return ArrayList<HashMap<String, String>>
     */
    public ArrayList<HashMap<String, String>> getErrors() {
        return errors;
    }

    /**
     * Check if any errors occured
     * @return boolean true if there were any errors
     */
    public boolean fails() {
        return this.errors.size() > 0;
    }
}
