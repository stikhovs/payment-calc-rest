package ru.payment.calc.payment_calculator.service;

import org.apache.poi.ss.util.CellAddress;
import ru.payment.calc.payment_calculator.model.Group;

import java.util.Map;

public interface InitWeekDaysService {

    void initWeekDays(Map<CellAddress, String> sheetInfoMap, Group group);

}
