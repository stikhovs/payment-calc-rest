package ru.payment.calc.payment_calculator.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;
import ru.payment.calc.payment_calculator.controller.dto.request.excel.ExcelDownloadRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupCalculationResponse;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupResponse;
import ru.payment.calc.payment_calculator.mapper.GroupMapper;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.GroupType;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;
import ru.payment.calc.payment_calculator.model.Student;
import ru.payment.calc.payment_calculator.props.DebtThreshold;
import ru.payment.calc.payment_calculator.service.MyExcelService;
import ru.payment.calc.payment_calculator.service.NextMonthHoursService;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.*;
import static org.apache.commons.collections4.CollectionUtils.containsAny;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyController {

    private final GroupMapper groupMapper;
    private final NextMonthHoursService nextMonthHoursService;
    private final DebtThreshold debtThreshold;
    private final MyExcelService excelService;

    @PostMapping("/process-groups")
    public GroupCalculationResponse processGroups(@RequestBody GroupCalculationRequest groupCalculationRequest) {

        NextMonthDatesStore nextMonthDatesStore = NextMonthDatesStore.builder()
                .nextMonthDate(groupCalculationRequest.getDateToCalc())
                .daysOff(new HashSet<>(groupCalculationRequest.getDaysOff()))
                .datesToChange(groupCalculationRequest.getDaysChange()
                        .stream()
                        .map(dayChange -> Pair.of(dayChange.getFrom(), dayChange.getTo()))
                        .collect(Collectors.toSet()))
                .build();

        Map<GroupType, List<GroupResponse>> groups = groupCalculationRequest.getGroups()
                .stream()
                .map(groupMapper::toGroup)
                .peek(group -> group.setNextMonthHours(nextMonthHoursService.calcNextMonthHours(group, nextMonthDatesStore)))
                .peek(group -> {
                    if (group.isIndividual()) {
                        group.setGroupType(GroupType.IND);
                    } else if (checkGroupForDays(group, MONDAY, WEDNESDAY, FRIDAY)) {
                        group.setGroupType(GroupType.MON_WED_FR);
                    } else if (checkGroupForDays(group, TUESDAY, THURSDAY)) {
                        group.setGroupType(GroupType.TUE_TH);
                    } else if (checkGroupForDays(group, SATURDAY)) {
                        group.setGroupType(GroupType.SAT);
                    } else {
                        group.setGroupType(GroupType.OTHER);
                    }
                })
                .peek(group -> group.getStudentsInfo()
                        .forEach(student -> {
                            double hoursToPay = group.getNextMonthHours() - student.getBalance();
                            if (hoursToPay > debtThreshold.getThreshold()) {
                                student.setHoursToPay(hoursToPay);
                            } else {
                                student.setHoursToPay(0.0);
                            }

                            if (student.getHoursToPay() > 0.0) {
                                student.setMoneyToPay(student.getHoursToPay() * getPriceForStudent(student, group.getPricePerHour()));
                            } else {
                                student.setMoneyToPay(0.0);
                            }
                        }))
                .map(groupMapper::toGroupResponse).collect(Collectors.groupingBy(GroupResponse::getGroupType));


        return new GroupCalculationResponse(
                groups.get(GroupType.MON_WED_FR),
                groups.get(GroupType.TUE_TH),
                groups.get(GroupType.SAT),
                groups.get(GroupType.IND),
                groups.get(GroupType.OTHER)
        );
    }


    @PostMapping("/download-excel")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody ExcelDownloadRequest request) {
        XSSFWorkbook excel = excelService.createExcel(request);
        return ResponseEntity.ok(toByteArray(excel));
    }


    private double getPriceForStudent(Student student, double groupPrice) {
        if (student.getDiscount() == 0) {
            return groupPrice;
        }
        return groupPrice - groupPrice * student.getDiscount();
    }

    private boolean checkGroupForDays(Group group, DayOfWeek... days) {
        if (isNotEmpty(group.getClassDaysOne()) && isNotEmpty(group.getClassDaysTwo())) {
            return containsAny(group.getClassDaysOne(), days) && containsAny(group.getClassDaysTwo(), days);
        }
        if (isNotEmpty(group.getClassDaysOne())) {
            return containsAny(group.getClassDaysOne(), days);
        }
        if (isNotEmpty(group.getClassDaysTwo())) {
            return containsAny(group.getClassDaysTwo(), days);
        }
        return false;
    }


    @SneakyThrows
    private byte[] toByteArray(XSSFWorkbook excel) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            excel.write(bos);
            return bos.toByteArray();
        }
    }
}
