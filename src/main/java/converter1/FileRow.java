package converter1;

import javafx.beans.property.SimpleStringProperty;

public class FileRow {

    private final SimpleStringProperty fileName;

    public FileRow(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
    }

    public String getFileName() {
        return fileName.get();
    }

    public SimpleStringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }
}
