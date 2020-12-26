package ru.payment.calc.payment_calculator.props;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.DayOfWeek.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeekDaysMaps {

    private final CellProps cellProps;

    @Bean
    public Map<DayOfWeek, CellAddress> weekDaysOneMap() {
        return initCellMap(cellProps.getWeekDaysOne().getStart(), cellProps.getWeekDaysOne().getEnd());
    }

    @Bean
    public Map<DayOfWeek, CellAddress> weekDaysTwoMap() {
        return initCellMap(cellProps.getWeekDaysTwo().getStart(), cellProps.getWeekDaysTwo().getEnd());
    }

    private Map<DayOfWeek, CellAddress> initCellMap(String start, String end) {
        List<CellAddress> weekDaysCells = new ArrayList<>();
        CellAddress startCellAddress = new CellAddress(start);
        CellAddress endCellAddress = new CellAddress(end);
        for (int i = 0; i <= endCellAddress.getColumn() - startCellAddress.getColumn(); i++) {
            CellAddress weekdayCellAddress = new CellAddress(startCellAddress.getRow(), startCellAddress.getColumn() + i);
            weekDaysCells.add(weekdayCellAddress);
        }
        if(CollectionUtils.isNotEmpty(weekDaysCells) && weekDaysCells.size() == 7) {
            return Map.of(
                    MONDAY, weekDaysCells.get(0),
                    TUESDAY, weekDaysCells.get(1),
                    WEDNESDAY, weekDaysCells.get(2),
                    THURSDAY, weekDaysCells.get(3),
                    FRIDAY, weekDaysCells.get(4),
                    SATURDAY, weekDaysCells.get(5),
                    SUNDAY, weekDaysCells.get(6)
            );
        }
        else {
            log.warn("Дни недели не проинициализированы. Возвращена пустая коллекция.");
            return Map.of();
        }
    }

}
