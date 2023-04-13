package ru.payment.calc.payment_calculator.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "debt")
@Data
public class DebtThreshold {

    private BigDecimal threshold;

}
