package ru.payment.calc.payment_calculator.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Profile(value = "!local")
@Configuration
@EnableScheduling
public class NoIdlingConfig {

    @Value("${backend.url}")
    private String backendUrl;

    @SneakyThrows
    @Scheduled(fixedRate = 600000L) // 10 min
    public void pingGoogle() {
        RestTemplate restTemplate = new RestTemplate();
        Object obj = restTemplate.getForObject(backendUrl + "/actuator/health", Object.class);
        log.info("Self ping {}", obj);
    }

}
