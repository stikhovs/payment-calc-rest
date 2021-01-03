package ru.payment.calc.payment_calculator.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExcelSheetEnum {

    MON_WED_FR ("пн ср птн"),
    TUE_TH ("вт чт"),
    SAT ("сб"),
    IND ("инд"),
    OTHER ("другое");

    private final String name;

}
