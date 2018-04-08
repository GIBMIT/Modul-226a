package models.schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Properties;

/**
 * The object to hold any data for the  database
 */
public class Database {
    private String database;
    private String host;
    private String username;
    private int port;
    private Connection connection;
    private Properties connectionProperties = new Properties();

    /**
     * Database Constructor
     * @param database String the name of the database
     * @param host String the host of the database
     * @param port int the port where the database is reachable
     * @param username String the username to authenticate on the database
     * @param password String the password for the username
     * @throws SQLException If Connection could not established
     */
    public Database(String database, String host, int port, String username, String password) throws SQLException {
        this.database = database;
        this.host = host;
        this.username = username;
        this.port = port;
        this.connectionProperties.setProperty("user", this.username);
        this.connectionProperties.setProperty("password", password);

        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?zeroDateTimeBehavior=convertToNull";
        try {
            this.connection = DriverManager.getConnection(url, this.connectionProperties);
        }catch (SQLTimeoutException e)  {
            e.printStackTrace();
        }
    }

    /**
     * Getter for the connection
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Get the database name
     * @return String
     */
    public String getDatabaseName() {
        return this.database;
    }

    /**
     * Get the database host
     * @return String
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Get the database port
     * @return int
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Get the username that is authenticated
     * @return String
     */
    public String getUsername() {
        return this.username;
    }
}
