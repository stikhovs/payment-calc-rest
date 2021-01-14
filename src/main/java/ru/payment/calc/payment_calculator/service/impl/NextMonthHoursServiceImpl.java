package ru.payment.calc.payment_calculator.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;
import ru.payment.calc.payment_calculator.service.NextMonthHoursService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NextMonthHoursServiceImpl implements NextMonthHoursService {

    @Override
    public double calcNextMonthHours(Group group, NextMonthDatesStore nextMonthDatesStore) {

        long nextMonthClassDaysOne = calcNextMonthDays(group.getClassDaysOne(), nextMonthDatesStore);
        long nextMonthClassDaysTwo = calcNextMonthDays(group.getClassDaysTwo(), nextMonthDatesStore);

        double classDurationOne = group.getClassDurationOne();
        double classDurationTwo = group.getClassDurationTwo();

        return nextMonthClassDaysOne * classDurationOne + nextMonthClassDaysTwo * classDurationTwo;
    }

    private long calcNextMonthDays(List<DayOfWeek> classDays, NextMonthDatesStore nextMonthDatesStore) {
        LocalDate nextMonthDate = nextMonthDatesStore.getNextMonthDate();
        LocalDate untilDateExclusive = nextMonthDate.plus(Period.ofMonths(1));

        Set<LocalDate> daysOff = nextMonthDatesStore.getDaysOff();
        Set<Pair<LocalDate, LocalDate>> datesToChange = nextMonthDatesStore.getDatesToChange();
        List<LocalDate> datesToRemove = getDatesToRemove(datesToChange);

        return nextMonthDate.datesUntil(untilDateExclusive)
                .filter(localDate -> classDays.contains(localDate.getDayOfWeek()))
                .filter(localDate -> !daysOff.contains(localDate))
                .map(localDate -> {
                    if (datesToRemove.contains(localDate)) {
                        return changeDate(datesToChange, localDate);
                    }
                    return localDate;
                })
                .count();
    }

    private List<LocalDate> getDatesToRemove(Set<Pair<LocalDate, LocalDate>> datesToChange) {
        return datesToChange
                .stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
    }

    private LocalDate changeDate(Set<Pair<LocalDate, LocalDate>> datesToChange, LocalDate localDate) {
        return datesToChange
                .stream()
                .filter(pair -> pair.getKey().equals(localDate))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Смена дат: пара не найдена!"))
                .getValue();
    }

}
