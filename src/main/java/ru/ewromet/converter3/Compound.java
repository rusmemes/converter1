package ru.ewromet.converter3;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Compound {

    private final SimpleIntegerProperty posNumber = new SimpleIntegerProperty();
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty material = new SimpleStringProperty();
    private final SimpleStringProperty materialBrand = new SimpleStringProperty();
    private final SimpleDoubleProperty thickness = new SimpleDoubleProperty();
    private final SimpleIntegerProperty n = new SimpleIntegerProperty();

    private final SimpleIntegerProperty yst = new SimpleIntegerProperty();
    private final SimpleIntegerProperty xst = new SimpleIntegerProperty();
    private final SimpleIntegerProperty ymin = new SimpleIntegerProperty();
    private final SimpleIntegerProperty xmin = new SimpleIntegerProperty();

    private final SimpleDoubleProperty yr = new SimpleDoubleProperty();
    private final SimpleDoubleProperty xr = new SimpleDoubleProperty();
    private final SimpleDoubleProperty sk = new SimpleDoubleProperty();
    private final SimpleDoubleProperty so = new SimpleDoubleProperty();

    private final SimpleBooleanProperty fullList = new SimpleBooleanProperty();

    public int getPosNumber() {
        return posNumber.get();
    }

    public void setPosNumber(int posNumber) {
        this.posNumber.set(posNumber);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
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

    public int getN() {
        return n.get();
    }

    public void setN(int count) {
        this.n.set(count);
    }

    public int getYst() {
        return yst.get();
    }

    public void setYst(int yst) {
        this.yst.set(yst);
    }

    public int getXst() {
        return xst.get();
    }

    public void setXst(int xst) {
        this.xst.set(xst);
    }

    public int getYmin() {
        return ymin.get();
    }

    public void setYmin(int ymin) {
        this.ymin.set(ymin);
    }

    public int getXmin() {
        return xmin.get();
    }

    public void setXmin(int xmin) {
        this.xmin.set(xmin);
    }

    public double getYr() {
        return yr.get();
    }

    public void setYr(double yr) {
        this.yr.set(yr);
    }

    public double getXr() {
        return xr.get();
    }

    public void setXr(double xr) {
        this.xr.set(xr);
    }

    public double getSk() {
        return sk.get();
    }

    public void setSk(double sk) {
        this.sk.set(sk);
    }

    public double getSo() {
        return so.get();
    }

    public void setSo(double so) {
        this.so.set(so);
    }

    public boolean isFullList() {
        return fullList.get();
    }

    public void setFullList(boolean fullList) {
        this.fullList.set(fullList);
    }

    public SimpleBooleanProperty fullListProperty() {
        return fullList;
    }
}
