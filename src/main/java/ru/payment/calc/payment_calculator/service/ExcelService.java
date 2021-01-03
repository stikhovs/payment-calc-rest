package ru.payment.calc.payment_calculator.service;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.payment.calc.payment_calculator.model.Group;

import java.util.List;

public interface ExcelService {

    XSSFWorkbook createExcel(List<Group> groups, String month);

}
