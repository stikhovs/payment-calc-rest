package ru.payment.calc.payment_calculator.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import ru.payment.calc.payment_calculator.model.SheetInfo;

import java.util.List;
import java.util.Map;

public interface ExcelReaderService {

    List<SheetInfo> readWorkbook(Workbook workbook);

}
