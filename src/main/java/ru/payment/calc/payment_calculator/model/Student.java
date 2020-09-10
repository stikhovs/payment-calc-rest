package ru.payment.calc.payment_calculator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private String name;
    private double balance;
    private boolean hasDebt;
    private double nextMonthHours;
    private boolean indGraphic;
    private double discount;
}
