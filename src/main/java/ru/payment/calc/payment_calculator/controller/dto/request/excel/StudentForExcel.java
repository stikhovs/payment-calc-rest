package ru.payment.calc.payment_calculator.controller.dto.request.excel;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentForExcel {
    private String name;
    private BigDecimal balance;
    private boolean indGraphic;
    private BigDecimal discount;
    private boolean hasDebt;
    private BigDecimal hoursToPay;
    private BigDecimal moneyToPay;
}
