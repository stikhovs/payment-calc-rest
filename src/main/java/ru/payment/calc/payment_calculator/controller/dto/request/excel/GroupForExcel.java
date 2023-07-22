package ru.payment.calc.payment_calculator.controller.dto.request.excel;

import lombok.Data;
import ru.payment.calc.payment_calculator.model.GroupType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Data
public class GroupForExcel {
    private String groupName;
    private BigDecimal pricePerHour;
    private String groupId;
    private String groupLevel;
    private String teacherOne;
    private String teacherTwo;
    private BigDecimal classDurationOne;
    private BigDecimal classDurationTwo;
    private String classStartTime;
    private List<DayOfWeek> classDaysOne;
    private List<DayOfWeek> classDaysTwo;
    private List<StudentForExcel> students;
    private boolean isIndividual;
    private BigDecimal nextMonthHours;
    private GroupType groupType;
}
