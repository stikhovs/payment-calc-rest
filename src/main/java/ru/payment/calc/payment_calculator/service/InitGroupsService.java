package ru.payment.calc.payment_calculator.service;

import org.apache.poi.ss.usermodel.Workbook;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;

import java.util.List;

public interface InitGroupsService {

    List<Group> init(Workbook workbook, NextMonthDatesStore nextMonthDatesStore);

}
