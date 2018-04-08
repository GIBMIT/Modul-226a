package controllers;

import exception.DatabaseNotFoundException;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.schema.Database;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class StartController extends AppController implements Initializable {

    @FXML
    private TextField database;
    @FXML
    private TextField host;
    @FXML
    private TextField port;
    @FXML
    private TextField username;
    @FXML
    private TextField password;

    @FXML
    private Label error;

    /**
     * Load database
     */
    @FXML
    public void loadDatabase() {
        if (this.validate()) {
            try {
                Database db = new Database(this.database.getText(),
                        this.host.getText(),
                        Integer.parseInt(this.port.getText()),
                        this.username.getText(),
                        this.password.getText());

                this.setDatabase(db);
                this.initDatabaseGUI();
            } catch (SQLException e) {
                this.error.setText("Database connection could not be established");
            } catch (IOException e) {
                this.error.setText("Oops, something went wrong. Please contact the developer. Code 2");
                e.printStackTrace();
            } catch (DatabaseNotFoundException e) {
                this.error.setText("Oops, something went wrong. Please contact the developer. Code 3");
                e.printStackTrace();
            }
        }
    }

    /**
     * Reset the database validation
     * @param event Event
     */
    @FXML
    public void resetValidation(Event event) {
        TextField t = (TextField) event.getTarget();
        t.setStyle("-fx-border-color: inherit;");
        this.error.setText("");
    }

    /**
     * Validate user Input
     *
     * @return boolean true if data is valid
     */
    private boolean validate() {
        boolean error = false;
        if (this.database.getText().length() <= 0) {
            this.setError(this.database);
            error = true;
        }

        if (this.host.getText().length() <= 0) {
            this.setError(this.host);
            error = true;
        }

        if (this.port.getText().length() <= 0) {
            this.setError(this.port);
        } else {
            int port = Integer.parseInt(this.port.getText());
            if (port <= 0 || port > 65535) {
                this.setError(this.port);
                error = true;
            }
        }

        if (this.username.getText().length() <= 0) {
            this.setError(this.username);
            error = true;
        }

        if (error) {
            this.error.setText("Please check your data");
        }
        return !error;
    }

    /**
     * Initialize method
     *
     * @param location  URL
     * @param resources ResourceBundle
     */
    public void initialize(URL location, ResourceBundle resources) {
        Database database = this.getDatabase();
        if (database == null) {
            try {
                database = new Database("information_schema", "localhost", 3306, "root", "");
            } catch (SQLException e) {
                this.error.setText("Initialization failed. Local database not found. Code 4");
                e.printStackTrace();
            }
        }
        try {
            this.database.setText(database.getDatabaseName());
            this.host.setText(database.getHost());
            this.port.setText(Integer.toString(database.getPort()));
            this.username.setText(database.getUsername());
        } catch (NullPointerException e) {
            this.database.setText("information_schema");
            this.host.setText("localhost");
            this.port.setText("3306");
            this.username.setText("root");
        }
    }
}
