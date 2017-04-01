package ru.ewromet.converter2.parser;

import java.util.List;

public class Attr {

    private String num;
    private String name;
    private String desc;
    private String type;
    private String ord;
    private String value;
    private List<Valid> valids;
    private List<MC> mcs;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrd() {
        return ord;
    }

    public void setOrd(String ord) {
        this.ord = ord;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Valid> getValids() {
        return valids;
    }

    public void setValids(List<Valid> valids) {
        this.valids = valids;
    }

    public List<MC> getMcs() {
        return mcs;
    }

    public void setMcs(List<MC> mcs) {
        this.mcs = mcs;
    }
}
