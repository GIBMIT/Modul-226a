package models.schema;

/**
 * A Database Column
 */
public class Column {
    private String name;
    private String type;
    private int length;
    private boolean nullable;
    private boolean isPrimary;
    private String defaultValue = null;
    private String extra = null;
    private String comment = null;
    private boolean isAutoIncrement;

    /**
     * Constructor
     * @param name String the table column name
     * @param type String the type of the column (e.g. INT or VARCHAR)
     * @param length int the length of the field in the database (size)
     * @param nullable boolean indicates, if the column is nullable
     * @param isPrimary boolean indicates, if the column is a primary key
     * @param isAutoIncrement boolean indicates, if a column is auto_increment
     * @param defaultValue String the default value of the column
     * @param extra String if the column has any extras (from information_schema)
     * @param comment String comment of the column
     */
    public Column(String name, String type, int length, boolean nullable, boolean isPrimary, boolean isAutoIncrement, String defaultValue, String extra, String comment) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.nullable = nullable;
        this.isPrimary = isPrimary;
        this.isAutoIncrement = isAutoIncrement;
        this.defaultValue = defaultValue;
        this.extra = extra;
        this.comment = comment;
    }

    /**
     * Get the column name
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get the column type
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Get the column length
     * @return int
     */
    public int  getLength() {
        return length;
    }

    /**
     * Check if the column is nullable
     * @return boolean
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * Check if the column is a primary key
     * @return boolean
     */
    public boolean isPrimary() {
        return isPrimary;
    }

    /**
     * Check if the column is AUTO_INCREMENT
     * @return boolean
     */
    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    /**
     * Get the column default value
     * @return String
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get the column extra (from information_schema)
     * @return String
     */
    public String getExtra() {
        return extra;
    }

    /**
     * Get the column comment
     * @return String
     */
    public String getComment() {
        return comment;
    }
}
