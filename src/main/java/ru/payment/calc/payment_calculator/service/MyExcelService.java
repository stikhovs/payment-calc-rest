package ru.payment.calc.payment_calculator.service;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.payment.calc.payment_calculator.controller.dto.request.excel.ExcelDownloadRequest;

public interface MyExcelService {

    XSSFWorkbook createExcel(ExcelDownloadRequest request);

}
