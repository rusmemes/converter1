package ru.ewromet.converter1;

public class OrderParserException extends RuntimeException {

    public OrderParserException(Object info) {
        super(info.toString());
    }

    OrderParserException(int pos, Object info) {
        super("Строка " + pos + ": " + info.toString());
    }
}
