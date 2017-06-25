package ru.ewromet.converter3;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CompoundAggregation {

    private final SimpleIntegerProperty posNumber = new SimpleIntegerProperty();
    private final SimpleStringProperty material = new SimpleStringProperty();
    private final SimpleStringProperty materialBrand = new SimpleStringProperty();
    private final SimpleDoubleProperty thickness = new SimpleDoubleProperty();
    private final SimpleDoubleProperty size = new SimpleDoubleProperty();
    private final SimpleIntegerProperty listsCount = new SimpleIntegerProperty();
    private final SimpleDoubleProperty totalConsumption = new SimpleDoubleProperty();
    private final SimpleDoubleProperty materialDensity = new SimpleDoubleProperty();
    private final SimpleDoubleProperty weight = new SimpleDoubleProperty();
    private final SimpleDoubleProperty price = new SimpleDoubleProperty();
    private final SimpleDoubleProperty totalPrice = new SimpleDoubleProperty();

    public int getPosNumber() {
        return posNumber.get();
    }

    public void setPosNumber(int posNumber) {
        this.posNumber.set(posNumber);
    }

    public String getMaterial() {
        return material.get();
    }

    public void setMaterial(String material) {
        this.material.set(material);
    }

    public String getMaterialBrand() {
        return materialBrand.get();
    }

    public void setMaterialBrand(String materialBrand) {
        this.materialBrand.set(materialBrand);
    }

    public double getThickness() {
        return thickness.get();
    }

    public void setThickness(double thickness) {
        this.thickness.set(thickness);
    }

    public double getSize() {
        return size.get();
    }

    public void setSize(double size) {
        this.size.set(size);
    }

    public int getListsCount() {
        return listsCount.get();
    }

    public void setListsCount(int listsCount) {
        this.listsCount.set(listsCount);
    }

    public double getTotalConsumption() {
        return totalConsumption.get();
    }

    public void setTotalConsumption(double totalConsumption) {
        this.totalConsumption.set(totalConsumption);
    }

    public double getMaterialDensity() {
        return materialDensity.get();
    }

    public void setMaterialDensity(double materialDensity) {
        this.materialDensity.set(materialDensity);
    }

    public double getWeight() {
        return weight.get();
    }

    public void setWeight(double weight) {
        this.weight.set(weight);
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice.set(totalPrice);
    }
}
