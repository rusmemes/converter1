package ru.ewromet.converter1;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.SimpleStringProperty;

public class OrderRow {

    private final SimpleStringProperty posNumber;
    private final SimpleStringProperty detailName;
    private final SimpleStringProperty count;
    private final SimpleStringProperty material;
    private final SimpleStringProperty materialBrand;
    private final SimpleStringProperty color;
    private final SimpleStringProperty thickness;
    private final SimpleStringProperty owner;
    private final SimpleStringProperty bendsCount;
    private final SimpleStringProperty drawCreation;
    private final SimpleStringProperty cleaning;
    private final SimpleStringProperty wasteReturn;
    private final SimpleStringProperty cuttingReturn;
    private final SimpleStringProperty comment;
    private final SimpleStringProperty relativeFilePath;

    public OrderRow() {
        this.posNumber = new SimpleStringProperty(StringUtils.EMPTY);
        this.detailName = new SimpleStringProperty(StringUtils.EMPTY);
        this.count = new SimpleStringProperty(StringUtils.EMPTY);
        this.material = new SimpleStringProperty(StringUtils.EMPTY);
        this.materialBrand = new SimpleStringProperty(StringUtils.EMPTY);
        this.color = new SimpleStringProperty(StringUtils.EMPTY);
        this.thickness = new SimpleStringProperty(StringUtils.EMPTY);
        this.owner = new SimpleStringProperty(StringUtils.EMPTY);
        this.bendsCount = new SimpleStringProperty(StringUtils.EMPTY);
        this.drawCreation = new SimpleStringProperty(StringUtils.EMPTY);
        this.cleaning = new SimpleStringProperty(StringUtils.EMPTY);
        this.wasteReturn = new SimpleStringProperty(StringUtils.EMPTY);
        this.cuttingReturn = new SimpleStringProperty(StringUtils.EMPTY);
        this.comment = new SimpleStringProperty(StringUtils.EMPTY);
        this.relativeFilePath = new SimpleStringProperty(StringUtils.EMPTY);
    }

    public OrderRow(String posNumber, String detailName, String count, String material, String materialBrand,
                    String color, String owner, String bendsCount, String comment) {
        this();
        this.posNumber.set(posNumber);
        this.detailName.set(detailName);
        this.count.set(count);
        this.material.set(material);
        this.materialBrand.set(materialBrand);
        this.color.set(color);
        this.owner.set(owner);
        this.bendsCount.set(bendsCount);
        this.comment.set(comment);
    }

    public String getPosNumber() {
        return posNumber.get();
    }

    public SimpleStringProperty posNumberProperty() {
        return posNumber;
    }

    public void setPosNumber(String posNumber) {
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

    public String getCount() {
        return count.get();
    }

    public SimpleStringProperty countProperty() {
        return count;
    }

    public void setCount(String count) {
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

    public String getThickness() {
        return thickness.get();
    }

    public SimpleStringProperty thicknessProperty() {
        return thickness;
    }

    public void setThickness(String thickness) {
        this.thickness.set(thickness);
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

    public String getBendsCount() {
        return bendsCount.get();
    }

    public SimpleStringProperty bendsCountProperty() {
        return bendsCount;
    }

    public void setBendsCount(String bendsCount) {
        this.bendsCount.set(bendsCount);
    }

    public String getDrawCreation() {
        return drawCreation.get();
    }

    public SimpleStringProperty drawCreationProperty() {
        return drawCreation;
    }

    public void setDrawCreation(String drawCreation) {
        this.drawCreation.set(drawCreation);
    }

    public String getCleaning() {
        return cleaning.get();
    }

    public SimpleStringProperty cleaningProperty() {
        return cleaning;
    }

    public void setCleaning(String cleaning) {
        this.cleaning.set(cleaning);
    }

    public String getWasteReturn() {
        return wasteReturn.get();
    }

    public SimpleStringProperty wasteReturnProperty() {
        return wasteReturn;
    }

    public void setWasteReturn(String wasteReturn) {
        this.wasteReturn.set(wasteReturn);
    }

    public String getCuttingReturn() {
        return cuttingReturn.get();
    }

    public SimpleStringProperty cuttingReturnProperty() {
        return cuttingReturn;
    }

    public void setCuttingReturn(String cuttingReturn) {
        this.cuttingReturn.set(cuttingReturn);
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

    public String getRelativeFilePath() {
        return relativeFilePath.get();
    }

    public SimpleStringProperty relativeFilePathProperty() {
        return relativeFilePath;
    }

    public void setRelativeFilePath(String relativeFilePath) {
        this.relativeFilePath.set(relativeFilePath);
    }
}
