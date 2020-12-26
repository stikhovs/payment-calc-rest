package ru.payment.calc.payment_calculator.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @Test
    public void test() {
        double result = Utils.parseStringToDouble("-10,00");
        assertEquals(-10.00, result);
    }

}
