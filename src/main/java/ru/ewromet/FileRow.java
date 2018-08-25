package ru.ewromet;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileRow {

    private final SimpleIntegerProperty posNumber = new SimpleIntegerProperty();
    private final SimpleStringProperty filePath = new SimpleStringProperty();

    public FileRow() {
    }

    public FileRow(String filePath) {
        this.filePath.set(filePath);
    }

    public int getPosNumber() {
        return posNumber.get();
    }

    public void setPosNumber(int posNumber) {
        this.posNumber.set(posNumber);
    }

    public String getFilePath() {
        return filePath.get();
    }

    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
    }

    @Override
    public int hashCode() {
        int result = posNumber.hashCode();
        result = 31 * result + filePath.hashCode();
        return result;
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

        if (!posNumber.equals(fileRow.posNumber)) {
            return false;
        }
        if (!filePath.equals(fileRow.filePath)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return posNumber.get() + " | " + filePath.get();
    }
}
