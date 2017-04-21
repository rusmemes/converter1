package ru.ewromet.converter2;

public class SymFileInfo {

    private double cutLength;
    private int insertsCount;
    private double actualArea;
    private double areaWithInternalContours;
    private double sizeX;
    private double sizeY;
    private double cutTimeUniMach;
    private double cutTimeTrumpf;

    public double getCutLength() {
        return cutLength;
    }

    public void setCutLength(double cutLength) {
        this.cutLength = cutLength;
    }

    public int getInsertsCount() {
        return insertsCount;
    }

    public void setInsertsCount(int insertsCount) {
        this.insertsCount = insertsCount;
    }

    public double getActualArea() {
        return actualArea;
    }

    public void setActualArea(double actualArea) {
        this.actualArea = actualArea;
    }

    public double getAreaWithInternalContours() {
        return areaWithInternalContours;
    }

    public void setAreaWithInternalContours(double areaWithInternalContours) {
        this.areaWithInternalContours = areaWithInternalContours;
    }

    public double getSizeX() {
        return sizeX;
    }

    public void setSizeX(double sizeX) {
        this.sizeX = sizeX;
    }

    public double getSizeY() {
        return sizeY;
    }

    public void setSizeY(double sizeY) {
        this.sizeY = sizeY;
    }

    public double getCutTimeUniMach() {
        return cutTimeUniMach;
    }

    public void setCutTimeUniMach(double cutTimeUniMach) {
        this.cutTimeUniMach = cutTimeUniMach;
    }

    public double getCutTimeTrumpf() {
        return cutTimeTrumpf;
    }

    public void setCutTimeTrumpf(double cutTimeTrumpf) {
        this.cutTimeTrumpf = cutTimeTrumpf;
    }
}
