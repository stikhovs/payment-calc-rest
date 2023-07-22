package ru.payment.calc.payment_calculator.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthCheckController {


    @GetMapping("/health-check")
    public HealthCheck healthCheck() {
        log.info("Healthcheck from frontend");
        return new HealthCheck("UP");
    }


    private record HealthCheck(String status){}

}
