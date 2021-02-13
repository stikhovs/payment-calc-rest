package ru.payment.calc.payment_calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class PaymentCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentCalculatorApplication.class, args);
    }

}
