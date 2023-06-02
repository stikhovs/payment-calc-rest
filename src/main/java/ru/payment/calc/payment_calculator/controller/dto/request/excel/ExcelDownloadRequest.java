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


    @Override
    public String toString() {
        return String.format("ExcelDownloadRequest{" +
                "Month: %s; monWedFr groups number: %d; tueThr groups number: %d; sat groups number: %d; individuals number: %d; other groups number: %d}",
                getMonth(), getMonWedFr().size(), getTueThr().size(), getSat().size(), getIndividuals().size(), getOthers().size());
    }
}
