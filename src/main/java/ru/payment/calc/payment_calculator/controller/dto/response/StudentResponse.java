package ru.payment.calc.payment_calculator.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudentResponse {
    private String name;
    private BigDecimal balance;
    private boolean indGraphic;
    private BigDecimal discount;
    private boolean hasDebt;
    private BigDecimal hoursToPay;
    private BigDecimal moneyToPay;
}
