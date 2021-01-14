package ru.payment.calc.payment_calculator.service;

import org.apache.poi.ss.usermodel.Workbook;
import ru.payment.calc.payment_calculator.model.SheetInfo;

import java.util.List;

public interface ExcelReaderService {

    List<SheetInfo> readWorkbook(Workbook workbook);

}
