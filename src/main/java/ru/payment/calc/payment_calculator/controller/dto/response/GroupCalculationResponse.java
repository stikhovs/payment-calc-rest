package ru.payment.calc.payment_calculator.controller.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class GroupCalculationResponse {

    private final List<GroupResponse> monWedFr;
    private final List<GroupResponse> tueThr;
    private final List<GroupResponse> sat;
    private final List<GroupResponse> individuals;
    private final List<GroupResponse> others;

}
