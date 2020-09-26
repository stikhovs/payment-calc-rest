package ru.payment.calc.payment_calculator.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Student {
    private String name;
    private double balance;
    private boolean indGraphic;
    private boolean hasDebt;
    private double nextMonthHours;
    private double discount;
}
