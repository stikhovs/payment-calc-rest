package ru.payment.calc.payment_calculator.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "debt")
@Data
public class DebtThreshold {

    public double threshold;

}
