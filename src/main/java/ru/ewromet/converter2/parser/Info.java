package ru.ewromet.converter2.parser;

import java.util.List;

public class Info {

    private String num;
    private String name;
    private String value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<MC> getMcs() {
        return mcs;
    }

    public void setMcs(List<MC> mcs) {
        this.mcs = mcs;
    }
}
