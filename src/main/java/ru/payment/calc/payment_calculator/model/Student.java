package ru.payment.calc.payment_calculator.model;

import lombok.Data;

@Data
public class Student {
    private String name;
    private double balance;
    private boolean indGraphic;
    private boolean hasDebt;
    private double hoursToPay;
    private double moneyToPay;
    private double discount;
}
