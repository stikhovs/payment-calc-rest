package ru.payment.calc.payment_calculator.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "files")
@Data
public class FileProps {

    private String sourceWorkbookDirectory;

    private String resultWorkbookDirectory;

    private Long availableTimeToLive;

}
