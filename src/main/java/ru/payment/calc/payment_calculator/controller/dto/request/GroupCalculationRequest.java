package ru.payment.calc.payment_calculator.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.payment.calc.payment_calculator.controller.dto.common.DayChange;

import java.time.LocalDate;
import java.util.List;

@Data
public class GroupCalculationRequest {

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateToCalc;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private List<LocalDate> daysOff;

    private List<DayChange> daysChange;

    private List<GroupRequest> groups;
}
