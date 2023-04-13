package ru.payment.calc.payment_calculator.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Student {
    private String name;
    private BigDecimal balance;
    private boolean indGraphic;
    private boolean hasDebt;
    private BigDecimal hoursToPay;
    private BigDecimal moneyToPay;
    private BigDecimal discount;
}
