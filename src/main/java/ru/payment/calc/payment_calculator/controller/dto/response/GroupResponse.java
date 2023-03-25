package ru.payment.calc.payment_calculator.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.payment.calc.payment_calculator.model.GroupType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GroupResponse {
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
    private List<StudentResponse> students;
    private boolean isIndividual;
    private BigDecimal nextMonthHours;
    private GroupType groupType;
}
