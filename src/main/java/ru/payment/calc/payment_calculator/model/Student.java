package ru.payment.calc.payment_calculator.model;

import lombok.Builder;
import lombok.Data;

@Data
//@NoArgsConstructor
@Builder
public class Student {
    private String name;
    private double balance;
    private boolean hasDebt;
    private double nextMonthHours;
    private boolean indGraphic;
    private double discount;
}
