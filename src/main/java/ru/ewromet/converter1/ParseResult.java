package ru.ewromet.converter1;

import javafx.collections.ObservableList;

public class ParseResult {

    private ObservableList<OrderRow> orderRows;
    private String clientName;

    public ParseResult(ObservableList<OrderRow> orderRows, String clientName) {
        this.orderRows = orderRows;
        this.clientName = clientName;
    }

    public ObservableList<OrderRow> getOrderRows() {
        return orderRows;
    }

    public void setOrderRows(ObservableList<OrderRow> orderRows) {
        this.orderRows = orderRows;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
