package ru.ewromet;

@FunctionalInterface
public interface ExtendedConsumer<T> {

    void accept(T value) throws Exception;
}
