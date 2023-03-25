package ru.payment.calc.payment_calculator.controller.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DayChange {
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate from;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate to;
}
