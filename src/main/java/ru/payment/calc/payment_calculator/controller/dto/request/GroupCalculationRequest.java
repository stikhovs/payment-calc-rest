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


    @Override
    public String toString() {
        return String.format("GroupCalculationRequest{dateToCalc: %s; number of groups: %d; daysOff: %s; daysChange: %s}",
                getDateToCalc(), getGroups().size(), getDaysOff(), getDaysChange());
    }
}
