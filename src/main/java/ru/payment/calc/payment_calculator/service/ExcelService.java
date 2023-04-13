package ru.payment.calc.payment_calculator.service;

import ru.payment.calc.payment_calculator.controller.dto.request.excel.ExcelDownloadRequest;

public interface ExcelService {

    byte[] createExcel(ExcelDownloadRequest request);

}
