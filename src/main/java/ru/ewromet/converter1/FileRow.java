package ru.ewromet.converter1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileRow {

    private final SimpleIntegerProperty posNumber = new SimpleIntegerProperty();
    private final SimpleStringProperty relativeFilePath = new SimpleStringProperty();

    public FileRow() {}
    public FileRow(String relativeFilePath) {
        this.relativeFilePath.set(relativeFilePath);
    }

    public FileRow(Integer posNumber, String relativeFilePath) {
        this();
        this.posNumber.set(posNumber);
        this.relativeFilePath.set(relativeFilePath);
    }

    public int getPosNumber() {
        return posNumber.get();
    }

    public SimpleIntegerProperty posNumberProperty() {
        return posNumber;
    }

    public void setPosNumber(int posNumber) {
        this.posNumber.set(posNumber);
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
        return posNumber.get()  + " | " + relativeFilePath.get();
    }
}
