package ru.ewromet.converter3;

import java.util.Objects;

import org.apache.commons.lang3.tuple.Triple;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CompoundAggregation {

    private final SimpleIntegerProperty posNumber = new SimpleIntegerProperty();
    private final SimpleStringProperty material = new SimpleStringProperty();
    private final SimpleStringProperty materialEn = new SimpleStringProperty();
    private final SimpleStringProperty materialBrand = new SimpleStringProperty();
    private final SimpleDoubleProperty thickness = new SimpleDoubleProperty();
    private final SimpleDoubleProperty size = new SimpleDoubleProperty();
    private final SimpleIntegerProperty listsCount = new SimpleIntegerProperty();
    private final SimpleDoubleProperty totalConsumption = new SimpleDoubleProperty();
    private final SimpleDoubleProperty materialDensity = new SimpleDoubleProperty();
    private final SimpleDoubleProperty weight = new SimpleDoubleProperty();
    private final SimpleDoubleProperty price = new SimpleDoubleProperty();
    private final SimpleDoubleProperty totalPrice = new SimpleDoubleProperty();
    private final SimpleStringProperty xMin_x_yMin_m = new SimpleStringProperty();
    private final SimpleStringProperty xSt_x_ySt_m = new SimpleStringProperty();
    private Triple<Double, String, String> materialTriple;

    public int getPosNumber() {
        return posNumber.get();
    }

    public void setPosNumber(int posNumber) {
        this.posNumber.set(posNumber);
    }

    public String getMaterialEn() {
        return materialEn.get();
    }

    public void setMaterialEn(String materialEn) {
        this.materialEn.set(materialEn);
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

    public String getXMin_x_yMin_m() {
        return xMin_x_yMin_m.get();
    }

    public void setXMin_x_yMin_m(String xMin_x_yMin_m) {
        this.xMin_x_yMin_m.set(xMin_x_yMin_m);
    }

    public String getXSt_x_ySt_m() {
        return xSt_x_ySt_m.get();
    }

    public void setXSt_x_ySt_m(String xSt_x_ySt_m) {
        this.xSt_x_ySt_m.set(xSt_x_ySt_m);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posNumber, material, materialBrand, thickness, size, listsCount, totalConsumption, materialDensity, weight, price, totalPrice, xMin_x_yMin_m, xSt_x_ySt_m);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompoundAggregation that = (CompoundAggregation) o;
        return Objects.equals(posNumber, that.posNumber) &&
                Objects.equals(material, that.material) &&
                Objects.equals(materialBrand, that.materialBrand) &&
                Objects.equals(thickness, that.thickness) &&
                Objects.equals(size, that.size) &&
                Objects.equals(listsCount, that.listsCount) &&
                Objects.equals(totalConsumption, that.totalConsumption) &&
                Objects.equals(materialDensity, that.materialDensity) &&
                Objects.equals(weight, that.weight) &&
                Objects.equals(price, that.price) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(xMin_x_yMin_m, that.xMin_x_yMin_m) &&
                Objects.equals(xSt_x_ySt_m, that.xSt_x_ySt_m);
    }

    public Triple<Double, String, String> materialTriple() {
        if (materialTriple == null) {
            materialTriple = Triple.of(getThickness(), getMaterial(), getMaterialBrand());
        }
        return materialTriple;
    }

    public double getThickness() {
        return thickness.get();
    }

    public void setThickness(double thickness) {
        this.thickness.set(thickness);
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
}
