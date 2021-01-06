package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;
import ru.payment.calc.payment_calculator.model.SheetInfo;
import ru.payment.calc.payment_calculator.model.Student;
import ru.payment.calc.payment_calculator.props.*;
import ru.payment.calc.payment_calculator.service.*;
import ru.payment.calc.payment_calculator.utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static ru.payment.calc.payment_calculator.utils.Utils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitGroupsServiceImpl implements InitGroupsService {

    private final CellProps cellProps;
    private final IndividualProps individualProps;

    private final ExcelReaderService excelReaderService;
    private final InitWeekDaysService initWeekDaysService;
    private final InitStudentsService initStudentsService;
    private final NextMonthHoursService nextMonthHoursService;

    @Override
    public List<Group> init(Workbook workbook, NextMonthDatesStore nextMonthDatesStore) {
        ExecutorService executorService = Executors.
                newCachedThreadPool();

        List<Group> groups = excelReaderService.readWorkbook(workbook)
                .stream()
                .filter(SheetInfo::isValid)
                .map(sheetInfoMap -> supplyAsync(() -> mapSheetToGroup(sheetInfoMap, nextMonthDatesStore), executorService))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executorService.shutdown();

        return groups;
    }

    private Group mapSheetToGroup(SheetInfo sheetInfo, NextMonthDatesStore nextMonthDatesStore) {
        log.info("Now working on {}", sheetInfo.getSheetName());
        Group group = new Group();
        CellProps.GroupInfoCells groupInfoCells = cellProps.getGroupInfoCells();
        CellAddress pricePerHourCell = new CellAddress(groupInfoCells.getPricePerHour());
        CellAddress groupIdCell = new CellAddress(groupInfoCells.getGroupId());
        CellAddress groupLevelCell = new CellAddress(groupInfoCells.getGroupLevel());
        CellAddress teacherOneCell = new CellAddress(groupInfoCells.getTeacherOne());
        CellAddress teacherTwoCell = new CellAddress(groupInfoCells.getTeacherTwo());
        CellAddress classDurationOneCell = new CellAddress(groupInfoCells.getClassDurationOne());
        CellAddress classDurationTwoCell = new CellAddress(groupInfoCells.getClassDurationTwo());
        CellAddress classStartTimeCell = new CellAddress(groupInfoCells.getClassStartTime());

        group.setSheetName(sheetInfo.getSheetName());
        Map<CellAddress, String> sheetData = sheetInfo.getSheetData();
        nonNullSet(sheetData.get(pricePerHourCell), str -> group.setPricePerHour(parseStringToDouble(str)));
        if (group.getPricePerHour() > individualProps.getMinPrice()) {
            group.setIndividual(true);
        }
        nonNullSet(sheetData.get(groupIdCell), group::setGroupId);
        nonNullSet(sheetData.get(groupLevelCell), group::setGroupLevel);
        nonNullSet(sheetData.get(teacherOneCell), group::setTeacherOne);
        nonNullSet(sheetData.get(teacherTwoCell), group::setTeacherTwo);
        nonNullSet(sheetData.get(classDurationOneCell), str -> group.setClassDurationOne(Double.parseDouble(str)));
        nonNullSet(sheetData.get(classDurationTwoCell), str -> group.setClassDurationTwo(Double.parseDouble(str)));
        nonNullSet(sheetData.get(classStartTimeCell), group::setClassStartTime);

        initWeekDaysService.initWeekDays(sheetData, group);
        List<Student> studentList = initStudentsService.initStudents(sheetData);
        group.setStudentsInfo(studentList);

        double nextMonthHours = nextMonthHoursService.calcNextMonthHours(group, nextMonthDatesStore);
        group.setNextMonthHours(nextMonthHours);

        group.getStudentsInfo()
                .forEach(student -> {
                    student.setHoursToPay(group.getNextMonthHours() - student.getBalance());
                    student.setMoneyToPay(student.getHoursToPay() * getPriceForStudent(student, group.getPricePerHour()));
                });


        return group;
    }

    private double getPriceForStudent(Student student, double groupPrice) {
        if (student.getDiscount() == 0) {
            return groupPrice;
        }
        return groupPrice - groupPrice * student.getDiscount() / 100;
    }

}
