package ru.payment.calc.payment_calculator.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "individual")
@Data
public class IndividualProps {

    private double minPrice;

}
