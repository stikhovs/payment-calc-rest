package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.props.WeekDaysMaps;
import ru.payment.calc.payment_calculator.service.InitWeekDaysService;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

import static ru.payment.calc.payment_calculator.utils.Utils.getCell;
import static ru.payment.calc.payment_calculator.utils.Utils.toBooleanValue;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitWeekDaysServiceImpl implements InitWeekDaysService {

    private final WeekDaysMaps weekDaysMaps;

    @Override
    public void initWeekDays(XSSFSheet sheet, Group group) {
        handleClassDays(sheet, group.getClassDaysOne(), weekDaysMaps.weekDaysOneMap());
        handleClassDays(sheet, group.getClassDaysTwo(), weekDaysMaps.weekDaysTwoMap());
    }

    private void handleClassDays(XSSFSheet sheet, List<DayOfWeek> classDays, Map<DayOfWeek, CellAddress> dayCellMap) {
        dayCellMap.forEach((day, weekDayCell) ->
                getCell(sheet, weekDayCell)
                        .ifPresent(cell -> {
                            if (toBooleanValue(cell)) {
                                classDays.add(day);
                            }
                        })
        );
    }

}
