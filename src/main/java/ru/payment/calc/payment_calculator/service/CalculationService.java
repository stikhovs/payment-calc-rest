package ru.payment.calc.payment_calculator.service;

import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupCalculationResponse;

public interface CalculationService {

    GroupCalculationResponse calculate(GroupCalculationRequest request);

}
