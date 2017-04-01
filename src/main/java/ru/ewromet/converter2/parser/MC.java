package ru.ewromet.converter2.parser;

import java.util.List;

public class MC {

    private String machine;
    private String value;
    private List<Tool> tools;
    private List<Pierce> pierces;
    private List<CutCond> cutConds;

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    public List<Pierce> getPierces() {
        return pierces;
    }

    public void setPierces(List<Pierce> pierces) {
        this.pierces = pierces;
    }

    public List<CutCond> getCutConds() {
        return cutConds;
    }

    public void setCutConds(List<CutCond> cutConds) {
        this.cutConds = cutConds;
    }
}
