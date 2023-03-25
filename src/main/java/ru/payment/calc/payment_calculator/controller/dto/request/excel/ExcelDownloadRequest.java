package ru.payment.calc.payment_calculator.controller.dto.request.excel;

import lombok.Data;

import java.util.List;

@Data
public class ExcelDownloadRequest {

    private String month;
    private List<GroupForExcel> monWedFr;
    private List<GroupForExcel> tueThr;
    private List<GroupForExcel> sat;
    private List<GroupForExcel> individuals;
    private List<GroupForExcel> others;

}
