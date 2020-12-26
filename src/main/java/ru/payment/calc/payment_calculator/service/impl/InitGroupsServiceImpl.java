package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;
import ru.payment.calc.payment_calculator.model.Student;
import ru.payment.calc.payment_calculator.props.CellProps;
import ru.payment.calc.payment_calculator.service.InitGroupsService;
import ru.payment.calc.payment_calculator.service.InitStudentsService;
import ru.payment.calc.payment_calculator.service.InitWeekDaysService;
import ru.payment.calc.payment_calculator.service.NextMonthHoursService;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static ru.payment.calc.payment_calculator.utils.Utils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitGroupsServiceImpl implements InitGroupsService {

    private static final int YEAR = 2019;
    private static final Month MONTH = Month.MAY;

    private final CellProps cellProps;

    private final InitWeekDaysService initWeekDaysService;
    private final InitStudentsService initStudentsService;
    private final NextMonthHoursService nextMonthHoursService;

    @Override
    public List<Group> init(XSSFWorkbook workbook) {
        ExecutorService executorService = Executors.
                newCachedThreadPool();

        List<Group> result = getSheets(workbook)
                .stream()
                .map(sheet -> supplyAsync(() -> mapSheetToGroup(sheet), executorService))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .filter(group -> group.getPricePerHour() != 0.0)
                .collect(Collectors.toList());

        executorService.shutdown();

        return result;
    }

    private List<XSSFSheet> getSheets(XSSFWorkbook workbook) {
        log.info("Getting sheets");
        List<XSSFSheet> sheets = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetHidden(i)) {
                sheets.add(workbook.getSheetAt(i));
            }
        }
        return sheets;
    }

    private Group mapSheetToGroup(XSSFSheet sheet) {
        log.info("Now working on {}", sheet.getSheetName());

        Group group = new Group();
        CellAddress pricePerHourCell = new CellAddress(cellProps.getGroupInfoCells().getPricePerHour());
        CellAddress groupIdCell = new CellAddress(cellProps.getGroupInfoCells().getGroupId());
        CellAddress groupLevelCell = new CellAddress(cellProps.getGroupInfoCells().getGroupLevel());
        CellAddress teacherOneCell = new CellAddress(cellProps.getGroupInfoCells().getTeacherOne());
        CellAddress teacherTwoCell = new CellAddress(cellProps.getGroupInfoCells().getTeacherTwo());
        CellAddress classDurationOneCell = new CellAddress(cellProps.getGroupInfoCells().getClassDurationOne());
        CellAddress classDurationTwoCell = new CellAddress(cellProps.getGroupInfoCells().getClassDurationTwo());
        CellAddress classStartTimeCell = new CellAddress(cellProps.getGroupInfoCells().getClassStartTime());

        group.setSheetName(sheet.getSheetName());
        getCell(sheet, pricePerHourCell)
                .ifPresent(cell -> group.setPricePerHour(toDoubleValue(cell)));
        getCell(sheet, groupIdCell)
                .ifPresent(cell -> group.setGroupId(toStringValue(cell)));
        getCell(sheet, groupLevelCell)
                .ifPresent(cell -> group.setGroupLevel(toStringValue(cell)));
        getCell(sheet, teacherOneCell)
                .ifPresent(cell -> group.setTeacherOne(toStringValue(cell)));
        getCell(sheet, teacherTwoCell)
                .ifPresent(cell -> group.setTeacherTwo(toStringValue(cell)));
        getCell(sheet, classDurationOneCell)
                .ifPresent(cell -> group.setClassDurationOne(toDoubleValue(cell)));
        getCell(sheet, classDurationTwoCell)
                .ifPresent(cell -> group.setClassDurationTwo(toDoubleValue(cell)));
        getCell(sheet, classStartTimeCell)
                .ifPresent(cell -> group.setClassStartTime(toStringValue(cell)));

        initWeekDaysService.initWeekDays(sheet, group);
        List<Student> studentList = initStudentsService.initStudents(sheet);
        group.setStudentsInfo(studentList);

        NextMonthDatesStore nextMonthDatesStore = buildNextMonthDatesStore();
        double nextMonthHours = nextMonthHoursService.calcNextMonthHours(group, nextMonthDatesStore);
        group.setNextMonthHours(nextMonthHours);

        group
                .getStudentsInfo()
                .forEach(student -> {
                    student.setHoursToPay(group.getNextMonthHours() - student.getBalance());
                    student.setMoneyToPay(student.getHoursToPay() * getPriceForStudent(student, group.getPricePerHour()));
                });

        return group;
    }


    //TODO: Remove later
    private NextMonthDatesStore buildNextMonthDatesStore() {

        LocalDate nextMonthDate = LocalDate.of(YEAR, MONTH, 1);

        Set<LocalDate> daysOff = new HashSet<>();
        daysOff.add(LocalDate.of(YEAR, MONTH, 1));
        daysOff.add(LocalDate.of(YEAR, MONTH, 9));

        Set<Pair<LocalDate, LocalDate>> datesToChange = new HashSet<>();
        LocalDate minusDate = LocalDate.of(YEAR, MONTH, 2);
        LocalDate plusDate = LocalDate.of(YEAR, MONTH, 10);
        datesToChange.add(Pair.of(minusDate, plusDate));

        return NextMonthDatesStore.builder()
                .nextMonthDate(nextMonthDate)
                .daysOff(daysOff)
                .datesToChange(datesToChange)
                .build();
    }

    private double getPriceForStudent(Student student, double groupPrice) {
        if (student.getDiscount() == 0) {
            return groupPrice;
        }
        return groupPrice - groupPrice * student.getDiscount() / 100;
    }

}
