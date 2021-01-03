package ru.payment.calc.payment_calculator.service;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;

import java.util.List;

public interface InitGroupsService {

    List<Group> init(XSSFWorkbook workbook, NextMonthDatesStore nextMonthDatesStore);

}
