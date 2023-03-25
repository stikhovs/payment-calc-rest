package ru.payment.calc.payment_calculator.controller.dto.request;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record GroupsToExcelRequest(
        LocalDate dateToCalc,
        List<GroupToExcel> groups
) {
    public record GroupToExcel(
            String sheetName,
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
            List<StudentToExcel> students,
            boolean isIndividual,
            BigDecimal nextMonthHours
    ) {
        public record StudentToExcel(
                String name,
                BigDecimal balance,
                boolean indGraphic,
                boolean hasDebt,
                BigDecimal hoursToPay,
                BigDecimal moneyToPay,
                BigDecimal discount
        ) {
        }
    }
}
