package ru.payment.calc.payment_calculator.props;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.payment.calc.payment_calculator.utils.DayOfWeek;

import java.util.Map;

import static ru.payment.calc.payment_calculator.utils.DayOfWeek.*;

@Configuration
@RequiredArgsConstructor
public class WeekDaysMaps {

    private final CellProps cellProps;

    @Bean
    public Map<DayOfWeek, CellAddress> weekDaysOneMap() {
        return Map.of(
                MONDAY, new CellAddress(cellProps.getWeekDaysOne().getMonday()),
                TUESDAY, new CellAddress(cellProps.getWeekDaysOne().getTuesday()),
                WEDNESDAY, new CellAddress(cellProps.getWeekDaysOne().getWednesday()),
                THURSDAY, new CellAddress(cellProps.getWeekDaysOne().getThursday()),
                FRIDAY, new CellAddress(cellProps.getWeekDaysOne().getFriday()),
                SATURDAY, new CellAddress(cellProps.getWeekDaysOne().getSaturday()),
                SUNDAY, new CellAddress(cellProps.getWeekDaysOne().getSunday())
        );
    }

    @Bean
    public Map<DayOfWeek, CellAddress> weekDaysTwoMap() {
        return Map.of(
                MONDAY, new CellAddress(cellProps.getWeekDaysTwo().getMonday()),
                TUESDAY, new CellAddress(cellProps.getWeekDaysTwo().getTuesday()),
                WEDNESDAY, new CellAddress(cellProps.getWeekDaysTwo().getWednesday()),
                THURSDAY, new CellAddress(cellProps.getWeekDaysTwo().getThursday()),
                FRIDAY, new CellAddress(cellProps.getWeekDaysTwo().getFriday()),
                SATURDAY, new CellAddress(cellProps.getWeekDaysTwo().getSaturday()),
                SUNDAY, new CellAddress(cellProps.getWeekDaysTwo().getSunday())
        );
    }

}
