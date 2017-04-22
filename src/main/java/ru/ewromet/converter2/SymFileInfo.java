package ru.ewromet.converter2;

public class SymFileInfo {

    private int cutLength;
    private int insertsCount;
    private int actualArea;
    private int areaWithInternalContours;
    private int sizeX;
    private int sizeY;
    private double cutTimeUniMach;
    private double cutTimeTrumpf;

    public int getCutLength() {
        return cutLength;
    }

    public void setCutLength(int cutLength) {
        this.cutLength = cutLength;
    }

    public int getInsertsCount() {
        return insertsCount;
    }

    public void setInsertsCount(int insertsCount) {
        this.insertsCount = insertsCount;
    }

    public int getActualArea() {
        return actualArea;
    }

    public void setActualArea(int actualArea) {
        this.actualArea = actualArea;
    }

    public int getAreaWithInternalContours() {
        return areaWithInternalContours;
    }

    public void setAreaWithInternalContours(int areaWithInternalContours) {
        this.areaWithInternalContours = areaWithInternalContours;
    }

    public int getSizeX() {
        return sizeX;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setSizeY(int sizeY) {
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

    @Override
    public String toString() {
        return "SymFileInfo{" +
                "cutLength=" + cutLength +
                ", insertsCount=" + insertsCount +
                ", actualArea=" + actualArea +
                ", areaWithInternalContours=" + areaWithInternalContours +
                ", sizeX=" + sizeX +
                ", sizeY=" + sizeY +
                ", cutTimeUniMach=" + cutTimeUniMach +
                ", cutTimeTrumpf=" + cutTimeTrumpf +
                '}';
    }
}
