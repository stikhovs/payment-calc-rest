package ru.payment.calc.payment_calculator.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.payment.calc.payment_calculator.config.YamlPropertySourceFactory;


@Configuration
@ConfigurationProperties(prefix = "cells")
@PropertySource(value = "classpath:cells-properties.yml", factory = YamlPropertySourceFactory.class)
@Data
public class CellProps {

    private GroupInfoCells groupInfoCells;

    /*private final List<String> groupInfoCells;
    private final List<String> studentNameCells;
    private final List<String> studentBalanceCells;
    private final List<String> weekDaysOne;
    private final List<String> weekDaysTwo;
    private final List<String> indGraphicCells;
    private final String singleDiscountColumn;
    private final String permanentDiscountColumn;*/

    @Data
    public static class GroupInfoCells {
        private String pricePerHour;
        private String groupId;
        private String groupLevel;
        private String teacherOne;
        private String teacherTwo;
        private String classDurationOne;
        private String classDurationTwo;
        private String classStartTime;
    }

}