package ru.ewromet.converter1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class OrderRow extends FileRow {

    public static final Map<String, String> MATERIALS_LABELS = Collections.unmodifiableMap(new HashMap<String, String>(){{
        put("сталь_хк", "Mild Steel");
        put("сталь х/к", "Mild Steel");
        put("сталь_гк", "Mild Steel");
        put("сталь г/к", "Mild Steel");
        put("сталь_рифл", "Mild Steel");
        put("оцинковка", "Zintec");
        put("нерж_мат", "Stainless Steel");
        put("нерж.мат", "Stainless Steel");
        put("нерж_рифл", "Stainless Steel");
        put("нерж_зерк", "Stainless Steel Foil");
        put("нерж.зерк +пл", "Stainless Steel Foil");
        put("нерж.зерк", "Stainless Steel Foil");
        put("нерж. зеркало", "Stainless Steel Foil");
        put("нерж_шлиф", "Stainless Steel Foil");
        put("алюминий", "Aluminium");
        put("алюм_рифл", "Aluminium");
        put("латунь", "Brass");
        put("медь", "Copper");
        put("иное", "");
    }});

    private final SimpleStringProperty detailName = new SimpleStringProperty();
    private final SimpleIntegerProperty count = new SimpleIntegerProperty();
    private final SimpleStringProperty material = new SimpleStringProperty();
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

    public OrderRow() {}

    public OrderRow(Integer posNumber, String detailName, Integer count, String material, String materialBrand,
                    String color, String owner, Integer bendsCount, String comment, String relativeFilePath) {
        super(posNumber, relativeFilePath);
        this.detailName.set(detailName);
        this.count.set(count);
        this.material.set(material);
        this.materialBrand.set(materialBrand);
        this.color.set(color);
        this.owner.set(owner);
        this.bendsCount.set(bendsCount);
        this.comment.set(comment);
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

    public double getThickness() {
        return thickness.get();
    }

    public SimpleDoubleProperty thicknessProperty() {
        return thickness;
    }

    public void setThickness(double thickness) {
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

    public int getBendsCount() {
        return bendsCount.get();
    }

    public SimpleIntegerProperty bendsCountProperty() {
        return bendsCount;
    }

    public void setBendsCount(int bendsCount) {
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

    @Override
    public String toString() {
        return super.toString() + " | " + detailName.get();
    }

    public FileRow getAsFileRow() {
        return this;
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
