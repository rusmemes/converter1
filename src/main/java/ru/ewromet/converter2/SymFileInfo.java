package ru.ewromet.converter2;

public class SymFileInfo {

    private Integer cutLength;
    private Integer insertsCount;
    private Integer actualArea;
    private Integer areaWithInternalContours;
    private Integer sizeX;
    private Integer sizeY;
    private Double cutTimeUniMach;
    private Double cutTimeTrumpf;

    public Integer getCutLength() {
        return cutLength;
    }

    public void setCutLength(Integer cutLength) {
        this.cutLength = cutLength;
    }

    public Integer getInsertsCount() {
        return insertsCount;
    }

    public void setInsertsCount(Integer insertsCount) {
        this.insertsCount = insertsCount;
    }

    public Integer getActualArea() {
        return actualArea;
    }

    public void setActualArea(Integer actualArea) {
        this.actualArea = actualArea;
    }

    public Integer getAreaWithInternalContours() {
        return areaWithInternalContours;
    }

    public void setAreaWithInternalContours(Integer areaWithInternalContours) {
        this.areaWithInternalContours = areaWithInternalContours;
    }

    public Integer getSizeX() {
        return sizeX;
    }

    public void setSizeX(Integer sizeX) {
        this.sizeX = sizeX;
    }

    public Integer getSizeY() {
        return sizeY;
    }

    public void setSizeY(Integer sizeY) {
        this.sizeY = sizeY;
    }

    public Double getCutTimeUniMach() {
        return cutTimeUniMach;
    }

    public void setCutTimeUniMach(Double cutTimeUniMach) {
        this.cutTimeUniMach = cutTimeUniMach;
    }

    public Double getCutTimeTrumpf() {
        return cutTimeTrumpf;
    }

    public void setCutTimeTrumpf(Double cutTimeTrumpf) {
        this.cutTimeTrumpf = cutTimeTrumpf;
    }
}
