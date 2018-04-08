package models.schema;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Attribute Row
 * Use to edit any table
 * The class consist of Simple Properties and setters & getters for those (Simple Property getter, Property setter, Property getter)
 */
public class AttributeRow {
    private final SimpleStringProperty attributeName;
    private final SimpleStringProperty attributeType;
    private final SimpleIntegerProperty attributeLength;
    private final SimpleBooleanProperty isNullable;
    private final SimpleBooleanProperty isPrimaryKey;
    private final SimpleBooleanProperty isAutoIncrement;
    private final SimpleStringProperty attributeDefault;
    private final SimpleStringProperty attributeComment;
    private final String oldName;


    public AttributeRow(
            String attributeName,
            String attributeType,
            int attributeLength,
            boolean isNullable,
            boolean isPrimaryKey,
            boolean isAutoIncrement,
            String attributeDefault,
            String attributeComment
    ) {
        this.attributeName = new SimpleStringProperty(attributeName);
        this.oldName = attributeName;
        this.attributeType = new SimpleStringProperty(attributeType);
        this.attributeLength = new SimpleIntegerProperty(attributeLength);
        this.isNullable = new SimpleBooleanProperty(isNullable);
        this.isPrimaryKey = new SimpleBooleanProperty(isPrimaryKey);
        this.isAutoIncrement = new SimpleBooleanProperty(isAutoIncrement);
        this.attributeDefault = new SimpleStringProperty(attributeDefault);
        this.attributeComment = new SimpleStringProperty(attributeComment);
    }

    public void setAttributeName(String attributeName) {
        this.attributeName.set(attributeName);
    }

    public String getAttributeName() {
        return attributeName.get();
    }

    public SimpleStringProperty attributeNameProperty() {
        return attributeName;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType.set(attributeType);
    }

    public String getAttributeType() {
        return this.attributeType.get();
    }

    public SimpleStringProperty attributeTypeProperty() {
        return attributeType;
    }

    public void setAttributeLength(int length) {
        this.attributeLength.set(length);
    }

    public int getAttributeLength() {
        return this.attributeLength.get();
    }

    public SimpleIntegerProperty attributeLengthProperty() {
        return attributeLength;
    }

    public void setIsNullable(boolean isNullable) {
        this.isNullable.set(isNullable);
    }

    public boolean getIsNullable() {
        return this.isNullable.get();
    }

    public SimpleBooleanProperty isNullableProperty() {
        return isNullable;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey.set(isPrimaryKey);
    }

    public boolean getIsPrimaryKey() {
        return this.isPrimaryKey.get();
    }

    public SimpleBooleanProperty isPrimaryKeyProperty() {
        return isPrimaryKey;
    }

    public void setIsAutoIncrement(boolean isAutoIncrement) {
        this.isAutoIncrement.set(isAutoIncrement);
    }

    public boolean getIsAutoIncrement() {
        return this.isAutoIncrement.get();
    }

    public SimpleBooleanProperty isAutoIncrementProperty() {
        return isAutoIncrement;
    }

    public void setAttributeDefault(String attributeDefault) {
        this.attributeDefault.set(attributeDefault);
    }

    public String getAttributeDefault() {
        return this.attributeDefault.get();
    }

    public SimpleStringProperty attributeDefaultProperty() {
        return attributeDefault;
    }

    public void setAttributeComment(String attributeComment) {
        this.attributeComment.set(attributeComment);
    }

    public String getAttributeComment() {
        return attributeComment.get();
    }

    public SimpleStringProperty attributeCommentProperty() {
        return attributeComment;
    }

    public String getOldName() {
        return oldName;
    }
}