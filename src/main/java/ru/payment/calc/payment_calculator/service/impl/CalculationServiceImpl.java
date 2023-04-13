package ru.payment.calc.payment_calculator.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.config.props.DebtThreshold;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupCalculationResponse;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupResponse;
import ru.payment.calc.payment_calculator.mapper.GroupMapper;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.GroupType;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;
import ru.payment.calc.payment_calculator.model.Student;
import ru.payment.calc.payment_calculator.service.CalculationService;
import ru.payment.calc.payment_calculator.service.NextMonthHoursService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.*;
import static org.apache.commons.collections4.CollectionUtils.containsAny;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private final GroupMapper groupMapper;
    private final NextMonthHoursService nextMonthHoursService;
    private final DebtThreshold debtThreshold;


    @Override
    public GroupCalculationResponse calculate(GroupCalculationRequest request) {
        List<GroupResponse> groupResponses = calculateGroups(request);
        return toGroupCalculationResponse(groupResponses);
    }

    private List<GroupResponse> calculateGroups(GroupCalculationRequest request) {
        NextMonthDatesStore nextMonthDatesStore = getNextMonthDatesStore(request);

        return request.getGroups()
                .stream()
                .map(groupMapper::toGroup)
                .peek(group -> setNextMonthHours(nextMonthDatesStore, group))
                .peek(this::setGroupType)
                .peek(group -> group.getStudentsInfo()
                        .forEach(student -> {
                            setHoursToPay(group, student);
                            setMoneyToPay(group, student);
                        }))
                .map(groupMapper::toGroupResponse)
                .toList();
    }

    private NextMonthDatesStore getNextMonthDatesStore(GroupCalculationRequest request) {
        return NextMonthDatesStore.builder()
                .nextMonthDate(request.getDateToCalc())
                .daysOff(new HashSet<>(request.getDaysOff()))
                .datesToChange(request.getDaysChange()
                        .stream()
                        .map(dayChange -> Pair.of(dayChange.getFrom(), dayChange.getTo()))
                        .collect(Collectors.toSet()))
                .build();
    }

    private void setNextMonthHours(NextMonthDatesStore nextMonthDatesStore, Group group) {
        group.setNextMonthHours(nextMonthHoursService.calcNextMonthHours(group, nextMonthDatesStore));
    }

    private void setGroupType(Group group) {
        if (group.isIndividual()) {
            group.setGroupType(GroupType.IND);
        } else if (groupHasDays(group, MONDAY, WEDNESDAY, FRIDAY)) {
            group.setGroupType(GroupType.MON_WED_FR);
        } else if (groupHasDays(group, TUESDAY, THURSDAY)) {
            group.setGroupType(GroupType.TUE_TH);
        } else if (groupHasDays(group, SATURDAY)) {
            group.setGroupType(GroupType.SAT);
        } else {
            group.setGroupType(GroupType.OTHER);
        }
    }

    private boolean groupHasDays(Group group, DayOfWeek... days) {
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

    private void setHoursToPay(Group group, Student student) {
        BigDecimal hoursToPay = group.getNextMonthHours().subtract(student.getBalance());
        if (hoursToPay.compareTo(debtThreshold.getThreshold()) > 0) {
            student.setHoursToPay(hoursToPay.setScale(2, RoundingMode.HALF_UP));
        } else {
            student.setHoursToPay(BigDecimal.ZERO);
        }
    }

    private void setMoneyToPay(Group group, Student student) {
        if (student.getHoursToPay().compareTo(BigDecimal.ZERO) > 0) {
            student.setMoneyToPay(
                    student.getHoursToPay()
                            .multiply(getPriceForStudent(student, group.getPricePerHour()))
                            .setScale(2, RoundingMode.HALF_UP)
            );
        } else {
            student.setMoneyToPay(BigDecimal.ZERO);
        }
    }

    private BigDecimal getPriceForStudent(Student student, BigDecimal groupPrice) {
        if (student.getDiscount().compareTo(BigDecimal.ZERO) == 0) {
            return groupPrice;
        }

        return groupPrice.subtract(
                groupPrice.multiply(student.getDiscount())
        );
    }

    private GroupCalculationResponse toGroupCalculationResponse(List<GroupResponse> groups) {
        Map<GroupType, List<GroupResponse>> byGroupType = groups.stream()
                .collect(Collectors.groupingBy(GroupResponse::getGroupType));

        return new GroupCalculationResponse(
                byGroupType.get(GroupType.MON_WED_FR),
                byGroupType.get(GroupType.TUE_TH),
                byGroupType.get(GroupType.SAT),
                byGroupType.get(GroupType.IND),
                byGroupType.get(GroupType.OTHER)
        );
    }

}
