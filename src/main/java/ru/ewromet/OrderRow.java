package ru.ewromet;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class OrderRow extends FileRow {

    private final SimpleStringProperty detailName = new SimpleStringProperty();
    private final SimpleStringProperty detailResultName = new SimpleStringProperty();
    private final SimpleIntegerProperty count = new SimpleIntegerProperty();
    private final SimpleStringProperty material = new SimpleStringProperty();
    private final SimpleStringProperty originalMaterial = new SimpleStringProperty();
    private final SimpleStringProperty materialBrand = new SimpleStringProperty();
    private final SimpleStringProperty color = new SimpleStringProperty();
    private final SimpleDoubleProperty thickness = new SimpleDoubleProperty();
    private final SimpleStringProperty owner = new SimpleStringProperty();
    private final SimpleIntegerProperty bendsCount = new SimpleIntegerProperty();
    private final SimpleStringProperty drawCreation = new SimpleStringProperty();
    private final SimpleStringProperty cleaning = new SimpleStringProperty();
    private final SimpleStringProperty wasteReturn = new SimpleStringProperty();
    private final SimpleStringProperty cuttingReturn = new SimpleStringProperty();
    private final SimpleStringProperty comment = new SimpleStringProperty();

    public String getDetailName() {
        return detailName.get();
    }

    public void setDetailName(String detailName) {
        this.detailName.set(detailName);
    }

    public String getDetailResultName() {
        return detailResultName.get();
    }

    public void setDetailResultName(String detailResultName) {
        this.detailResultName.set(detailResultName);
    }

    public int getCount() {
        return count.get();
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    public String getMaterial() {
        return material.get();
    }

    public void setMaterial(String material) {
        this.material.set(material);
    }

    public String getOriginalMaterial() {
        return originalMaterial.get();
    }

    public void setOriginalMaterial(String originalMaterial) {
        this.originalMaterial.set(originalMaterial);
    }

    public String getMaterialBrand() {
        return materialBrand.get();
    }

    public void setMaterialBrand(String materialBrand) {
        this.materialBrand.set(materialBrand);
    }

    public String getColor() {
        return color.get();
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public double getThickness() {
        return thickness.get();
    }

    public void setThickness(double thickness) {
        this.thickness.set(thickness);
    }

    public String getOwner() {
        return owner.get();
    }

    public void setOwner(String owner) {
        this.owner.set(owner);
    }

    public int getBendsCount() {
        return bendsCount.get();
    }

    public void setBendsCount(int bendsCount) {
        this.bendsCount.set(bendsCount);
    }

    public String getDrawCreation() {
        return drawCreation.get();
    }

    public void setDrawCreation(String drawCreation) {
        this.drawCreation.set(drawCreation);
    }

    public String getCleaning() {
        return cleaning.get();
    }

    public void setCleaning(String cleaning) {
        this.cleaning.set(cleaning);
    }

    public String getWasteReturn() {
        return wasteReturn.get();
    }

    public void setWasteReturn(String wasteReturn) {
        this.wasteReturn.set(wasteReturn);
    }

    public String getCuttingReturn() {
        return cuttingReturn.get();
    }

    public void setCuttingReturn(String cuttingReturn) {
        this.cuttingReturn.set(cuttingReturn);
    }

    public String getComment() {
        return comment.get();
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    @Override
    public String toString() {
        return super.toString() + " | " + detailName.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        OrderRow orderRow = (OrderRow) o;

        if (detailName != null ? !detailName.equals(orderRow.detailName) : orderRow.detailName != null) {
            return false;
        }
        if (detailResultName != null ? !detailResultName.equals(orderRow.detailResultName) : orderRow.detailResultName != null) {
            return false;
        }
        if (count != null ? !count.equals(orderRow.count) : orderRow.count != null) {
            return false;
        }
        if (material != null ? !material.equals(orderRow.material) : orderRow.material != null) {
            return false;
        }
        if (materialBrand != null ? !materialBrand.equals(orderRow.materialBrand) : orderRow.materialBrand != null) {
            return false;
        }
        if (color != null ? !color.equals(orderRow.color) : orderRow.color != null) {
            return false;
        }
        if (thickness != null ? !thickness.equals(orderRow.thickness) : orderRow.thickness != null) {
            return false;
        }
        if (owner != null ? !owner.equals(orderRow.owner) : orderRow.owner != null) {
            return false;
        }
        if (bendsCount != null ? !bendsCount.equals(orderRow.bendsCount) : orderRow.bendsCount != null) {
            return false;
        }
        if (drawCreation != null ? !drawCreation.equals(orderRow.drawCreation) : orderRow.drawCreation != null) {
            return false;
        }
        if (cleaning != null ? !cleaning.equals(orderRow.cleaning) : orderRow.cleaning != null) {
            return false;
        }
        if (wasteReturn != null ? !wasteReturn.equals(orderRow.wasteReturn) : orderRow.wasteReturn != null) {
            return false;
        }
        if (cuttingReturn != null ? !cuttingReturn.equals(orderRow.cuttingReturn) : orderRow.cuttingReturn != null) {
            return false;
        }
        if (comment != null ? !comment.equals(orderRow.comment) : orderRow.comment != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (detailName != null ? detailName.hashCode() : 0);
        result = 31 * result + (detailResultName != null ? detailResultName.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (material != null ? material.hashCode() : 0);
        result = 31 * result + (materialBrand != null ? materialBrand.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (thickness != null ? thickness.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (bendsCount != null ? bendsCount.hashCode() : 0);
        result = 31 * result + (drawCreation != null ? drawCreation.hashCode() : 0);
        result = 31 * result + (cleaning != null ? cleaning.hashCode() : 0);
        result = 31 * result + (wasteReturn != null ? wasteReturn.hashCode() : 0);
        result = 31 * result + (cuttingReturn != null ? cuttingReturn.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }
}
