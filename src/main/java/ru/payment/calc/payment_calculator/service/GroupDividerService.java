package ru.payment.calc.payment_calculator.service;

import ru.payment.calc.payment_calculator.model.ExcelSheetEnum;
import ru.payment.calc.payment_calculator.model.Group;

import java.util.List;
import java.util.Map;

public interface GroupDividerService {

    Map<ExcelSheetEnum, List<Group>> divideGroupsBySheets(List<Group> groups);

}
