package ru.payment.calc.payment_calculator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupCalculationResponse;
import ru.payment.calc.payment_calculator.service.CalculationService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CalculationController {
    private final CalculationService calculationService;

    @PostMapping("/calculate")
    public GroupCalculationResponse processGroups(@RequestBody GroupCalculationRequest groupCalculationRequest) {
        return calculationService.calculate(groupCalculationRequest);
    }

}
