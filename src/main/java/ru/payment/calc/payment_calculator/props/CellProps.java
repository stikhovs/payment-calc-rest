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

    private WeekDaysOne weekDaysOne;

    private WeekDaysTwo weekDaysTwo;

    private StudentNames studentNames;

    private StudentBalance studentBalance;

    private IndividualGraphic individualGraphic;

    private SingleDiscount singleDiscount;

    private PermanentDiscount permanentDiscount;

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

    @Data
    public static class WeekDaysOne {
        private String start;
        private String end;
    }

    @Data
    public static class WeekDaysTwo {
        private String start;
        private String end;
    }

    @Data
    public static class StudentNames {
        private String start;
        private String end;
    }

    @Data
    public static class StudentBalance {
        private String start;
        private String end;
    }

    @Data
    public static class IndividualGraphic {
        private String start;
        private String end;
    }

    @Data
    public static class SingleDiscount {
        private String start;
        private String end;
    }

    @Data
    public static class PermanentDiscount {
        private String start;
        private String end;
    }



}
