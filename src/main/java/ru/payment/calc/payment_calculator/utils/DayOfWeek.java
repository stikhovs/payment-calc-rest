package ru.payment.calc.payment_calculator.utils;

public enum DayOfWeek {

    MONDAY("пн"),
    TUESDAY("вт"),
    WEDNESDAY("ср"),
    THURSDAY("чт"),
    FRIDAY("пт"),
    SATURDAY("сб"),
    SUNDAY("вс");

    private final String day;

    DayOfWeek(String day) {
        this.day = day;
    }

    public String getValue() {
        return this.day;
    }
}
