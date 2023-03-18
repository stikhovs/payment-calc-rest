package ru.payment.calc.payment_calculator.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record GroupCalculationRequest(
        @JsonFormat(pattern = "dd.MM.yyyy") LocalDate dateToCalc,
        @JsonFormat(pattern = "dd.MM.yyyy") List<LocalDate> daysOff,
        List<DayChange> daysChange,
        List<Group> groups
) {
    public record DayChange(
            @JsonFormat(pattern = "dd.MM.yyyy") LocalDate from,
            @JsonFormat(pattern = "dd.MM.yyyy") LocalDate to
    ) {
    }

    public record Group(
            String groupName,
            BigDecimal pricePerHour,
            String groupId,
            String groupLevel,
            String teacherOne,
            String teacherTwo,
            BigDecimal classDurationOne,
            BigDecimal classDurationTwo,
            String classStartTime,
            List<DayOfWeek> classDaysOne,
            List<DayOfWeek> classDaysTwo,
            List<Student> students
    ) {
        public record Student(
                String name,
                BigDecimal balance,
                boolean indGraphic,
                BigDecimal singleDiscount,
                BigDecimal permanentDiscount
        ) {
        }
    }
}
