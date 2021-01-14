package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.props.WeekDaysMaps;
import ru.payment.calc.payment_calculator.service.InitWeekDaysService;
import ru.payment.calc.payment_calculator.utils.Utils;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitWeekDaysServiceImpl implements InitWeekDaysService {

    private final WeekDaysMaps weekDaysMaps;

    @Override
    public void initWeekDays(Map<CellAddress, String> sheetInfoMap, Group group) {
        handleClassDays(sheetInfoMap, group.getClassDaysOne(), weekDaysMaps.weekDaysOneMap());
        handleClassDays(sheetInfoMap, group.getClassDaysTwo(), weekDaysMaps.weekDaysTwoMap());
    }

    private void handleClassDays(Map<CellAddress, String> sheetInfoMap, List<DayOfWeek> classDays, Map<DayOfWeek, CellAddress> dayCellMap) {
        dayCellMap.forEach((day, weekDayCell) ->
                Utils.nonNullSet(sheetInfoMap.get(weekDayCell), weekDayStr -> {
                    if (Boolean.parseBoolean(weekDayStr)) {
                        classDays.add(day);
                    }
                })
      );
    }

}
