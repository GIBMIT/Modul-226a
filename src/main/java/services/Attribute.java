package services;

public class Attribute {
    private String name;
    private String type;
    private int length;
    private boolean nullable;
    private boolean isPrimary;
    private String defaultValue = null;
    private String extra = null;
    private String comment = null;
    private boolean isAutoIncrement;

    public Attribute(String name, String type, int length, boolean nullable, boolean isPrimary, boolean isAutoIncrement, String defaultValue, String extra, String comment) {
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


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getExtra() {
        return extra;
    }

    public String getComment() {
        return comment;
    }
}
