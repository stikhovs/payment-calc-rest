package ru.payment.calc.payment_calculator.model.request;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.UUID;

@Data
@Builder
public class UploadRequest {

    private final UUID id;
    private final String fileName;
    private final String dateToCalc;
    private final String daysOff;
    private final String daysFrom;
    private final String daysTo;

    @Setter
    private String month;

    @Setter
    private int year;

}
