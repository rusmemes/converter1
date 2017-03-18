package order;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class OrderRow {

    private final SimpleIntegerProperty posNumber;
    private final SimpleStringProperty detailName;
    private final SimpleIntegerProperty count;
    private final SimpleStringProperty material;
    private final SimpleStringProperty materialBrand;
    private final SimpleStringProperty color;
    private final SimpleStringProperty owner;
    private final SimpleStringProperty bending;
    private final SimpleIntegerProperty bendsCount;
    private final SimpleStringProperty comment;

    public OrderRow(Integer posNumber, String detailName, Integer count, String material, String materialBrand,
                    String color, String owner, String bending, Integer bendsCount, String comment) {
        this.posNumber = new SimpleIntegerProperty(posNumber);
        this.detailName = new SimpleStringProperty(detailName);
        this.count = new SimpleIntegerProperty(count);
        this.material = new SimpleStringProperty(material);
        this.materialBrand = new SimpleStringProperty(materialBrand);
        this.color = new SimpleStringProperty(color);
        this.owner = new SimpleStringProperty(owner);
        this.bending = new SimpleStringProperty(bending);
        this.bendsCount = new SimpleIntegerProperty(bendsCount);
        this.comment = new SimpleStringProperty(comment);
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

    public String getDetailName() {
        return detailName.get();
    }

    public SimpleStringProperty detailNameProperty() {
        return detailName;
    }

    public void setDetailName(String detailName) {
        this.detailName.set(detailName);
    }

    public int getCount() {
        return count.get();
    }

    public SimpleIntegerProperty countProperty() {
        return count;
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    public String getMaterial() {
        return material.get();
    }

    public SimpleStringProperty materialProperty() {
        return material;
    }

    public void setMaterial(String material) {
        this.material.set(material);
    }

    public String getMaterialBrand() {
        return materialBrand.get();
    }

    public SimpleStringProperty materialBrandProperty() {
        return materialBrand;
    }

    public void setMaterialBrand(String materialBrand) {
        this.materialBrand.set(materialBrand);
    }

    public String getColor() {
        return color.get();
    }

    public SimpleStringProperty colorProperty() {
        return color;
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public String getOwner() {
        return owner.get();
    }

    public SimpleStringProperty ownerProperty() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner.set(owner);
    }

    public String getBending() {
        return bending.get();
    }

    public SimpleStringProperty bendingProperty() {
        return bending;
    }

    public void setBending(String bending) {
        this.bending.set(bending);
    }

    public int getBendsCount() {
        return bendsCount.get();
    }

    public SimpleIntegerProperty bendsCountProperty() {
        return bendsCount;
    }

    public void setBendsCount(int bendsCount) {
        this.bendsCount.set(bendsCount);
    }

    public String getComment() {
        return comment.get();
    }

    public SimpleStringProperty commentProperty() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }
}
