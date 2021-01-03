package ru.payment.calc.payment_calculator.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.ExcelSheetEnum;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.service.GroupDividerService;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.*;
import static java.time.DayOfWeek.SATURDAY;
import static org.apache.commons.collections4.CollectionUtils.containsAny;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static ru.payment.calc.payment_calculator.model.ExcelSheetEnum.*;

@Slf4j
@Service
public class GroupDividerServiceImpl implements GroupDividerService {

    @Override
    public Map<ExcelSheetEnum, List<Group>> divideGroupsBySheets(List<Group> groups) {
        List<Group> individuals = getIndividuals(groups);
        List<Group> monWedFr = getMonWedFr(groups);
        List<Group> tueTh = getTueTh(groups);
        List<Group> sat = getSat(groups);
        List<Group> others = getOthers(groups, individuals, monWedFr, tueTh, sat);

        return Map.of(
                MON_WED_FR, monWedFr,
                TUE_TH, tueTh,
                SAT, sat,
                IND, individuals,
                OTHER, others
        );
    }

    private List<Group> getIndividuals(List<Group> groups) {
        return groups
                .stream()
                .filter(Group::isIndividual)
                .collect(Collectors.toList());
    }

    private List<Group> getMonWedFr(List<Group> groups) {
        return groups
                .stream()
                .filter(group -> isFalse(group.isIndividual()))
                .filter(group -> checkGroupForDays(group, MONDAY, WEDNESDAY, FRIDAY))
                .collect(Collectors.toList());
    }

    private List<Group> getTueTh(List<Group> groups) {
        return groups
                .stream()
                .filter(group -> isFalse(group.isIndividual()))
                .filter(group -> checkGroupForDays(group, TUESDAY, THURSDAY))
                .collect(Collectors.toList());
    }

    private List<Group> getSat(List<Group> groups) {
        return groups
                .stream()
                .filter(group -> isFalse(group.isIndividual()))
                .filter(group -> checkGroupForDays(group, SATURDAY))
                .collect(Collectors.toList());
    }

    private List<Group> getOthers(List<Group> groups, List<Group> individuals, List<Group> monWedFr, List<Group> tueTh, List<Group> sat) {
        return groups
                .stream()
                .filter(group -> isFalse(individuals.contains(group)))
                .filter(group -> isFalse(monWedFr.contains(group)))
                .filter(group -> isFalse(tueTh.contains(group)))
                .filter(group -> isFalse(sat.contains(group)))
                .collect(Collectors.toList());
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
}
