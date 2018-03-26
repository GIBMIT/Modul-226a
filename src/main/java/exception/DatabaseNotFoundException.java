package exception;

public class DatabaseNotFoundException extends Exception {
    public DatabaseNotFoundException(String message) {
        super(message);
    }
}
