package ru.payment.calc.payment_calculator.model.request;

import lombok.Data;

@Data
public class CalculateRequest {
    private String fileName;
    private String dateToCalc;
    private String daysOff;
    private String daysFrom;
    private String daysTo;
}
