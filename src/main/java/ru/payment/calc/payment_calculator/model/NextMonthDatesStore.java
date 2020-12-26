package ru.payment.calc.payment_calculator.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Builder
public class NextMonthDatesStore {

    private final LocalDate nextMonthDate;

    private final Set<LocalDate> daysOff;

    private final Set<Pair<LocalDate, LocalDate>> datesToChange;

}
