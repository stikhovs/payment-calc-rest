package ru.payment.calc.payment_calculator.model;

import lombok.Data;
import org.apache.poi.ss.util.CellAddress;

import java.util.Map;

@Data
public class SheetInfo {

    private String sheetName;

    private Map<CellAddress, String> sheetData;

    private boolean valid;

}
