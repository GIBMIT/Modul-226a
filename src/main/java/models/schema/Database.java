package models.schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private String database;
    private String host;
    private String username;
    private int port;
    private Connection connection;
    private Properties connectionProperties = new Properties();

    public Database(String database, String host, int port, String username, String password) throws SQLException, ClassNotFoundException {
        this.database = database;
        this.host = host;
        this.username = username;
        this.port = port;
        this.connectionProperties.setProperty("user", this.username);
        this.connectionProperties.setProperty("password", password);

        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?zeroDateTimeBehavior=convertToNull";
        this.connection = DriverManager.getConnection(url, this.connectionProperties);
    }

    public Connection getConnection() {
        return connection;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }
}
