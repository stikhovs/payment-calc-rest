package ru.payment.calc.payment_calculator.service;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.payment.calc.payment_calculator.model.Group;

public interface InitWeekDaysService {

    void initWeekDays(XSSFSheet sheet, Group group);

}
