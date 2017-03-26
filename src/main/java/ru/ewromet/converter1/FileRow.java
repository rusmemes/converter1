package ru.ewromet.converter1;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileRow {

    private final SimpleStringProperty stringPosNumber = new SimpleStringProperty(StringUtils.EMPTY);
    private final SimpleIntegerProperty posNumber = new SimpleIntegerProperty();
    private final SimpleStringProperty relativeFilePath = new SimpleStringProperty();

    public FileRow() {
    }

    public FileRow(String relativeFilePath) {
        this.posNumber.set(Integer.MAX_VALUE);
        this.relativeFilePath.set(relativeFilePath);
    }

    public FileRow(Integer posNumber, String relativeFilePath) {
        this();
        this.posNumber.set(posNumber);
        this.stringPosNumber.set(posNumber == null ? "" : posNumber.toString());
        this.relativeFilePath.set(relativeFilePath);
    }

    public String getStringPosNumber() {
        return stringPosNumber.get();
    }

    public SimpleStringProperty stringPosNumberProperty() {
        return stringPosNumber;
    }

    public void setStringPosNumber(String stringPosNumber) {
        this.stringPosNumber.set(stringPosNumber);
        try {
            this.posNumber.set(Integer.valueOf(stringPosNumber));
        } catch (NumberFormatException e) {
            this.posNumber.set(Integer.MAX_VALUE);
        }
    }

    public int getPosNumber() {
        return posNumber.get();
    }

    public SimpleIntegerProperty posNumberProperty() {
        return posNumber;
    }

    public void setPosNumber(int posNumber) {
        this.posNumber.set(posNumber);
        this.stringPosNumber.set(String.valueOf(posNumber));
    }

    public String getRelativeFilePath() {
        return relativeFilePath.get();
    }

    public SimpleStringProperty relativeFilePathProperty() {
        return relativeFilePath;
    }

    public void setRelativeFilePath(String relativeFilePath) {
        this.relativeFilePath.set(relativeFilePath);
    }

    @Override
    public String toString() {
        return posNumber.get() + " | " + relativeFilePath.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileRow fileRow = (FileRow) o;

        if (stringPosNumber != null ? !stringPosNumber.equals(fileRow.stringPosNumber) : fileRow.stringPosNumber != null) {
            return false;
        }
        if (posNumber != null ? !posNumber.equals(fileRow.posNumber) : fileRow.posNumber != null) {
            return false;
        }
        if (relativeFilePath != null ? !relativeFilePath.equals(fileRow.relativeFilePath) : fileRow.relativeFilePath != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = stringPosNumber != null ? stringPosNumber.hashCode() : 0;
        result = 31 * result + (posNumber != null ? posNumber.hashCode() : 0);
        result = 31 * result + (relativeFilePath != null ? relativeFilePath.hashCode() : 0);
        return result;
    }
}
