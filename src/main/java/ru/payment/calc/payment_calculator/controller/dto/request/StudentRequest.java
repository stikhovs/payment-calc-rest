package ru.payment.calc.payment_calculator.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudentRequest {
    private String name;
    private BigDecimal balance;
    private boolean indGraphic;
    private BigDecimal singleDiscount;
    private BigDecimal permanentDiscount;
}
