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

    private static final int YEAR = 2019;
    private static final Month MONTH = Month.MAY;

    private final CellProps cellProps;
    private final IndividualProps individualProps;

    private final InitWeekDaysService initWeekDaysService;
    private final InitStudentsService initStudentsService;
    private final NextMonthHoursService nextMonthHoursService;
    private final WeekDaysMaps weekDaysMaps;
    private final StudentInfoMaps studentInfoMaps;
    private final GroupInfoMaps groupInfoMaps;

    private final ExcelReaderService excelReaderService;

    @Override
    public List<Group> init(Workbook workbook, NextMonthDatesStore nextMonthDatesStore) {
        /*ExecutorService executorService = Executors.
                newFixedThreadPool(1);*/

        return excelReaderService.readWorkbook(workbook)
                .stream()
                .filter(SheetInfo::isValid)
                .peek(System.out::println)
                .map(sheetInfoMap -> mapSheetToGroup(sheetInfoMap, nextMonthDatesStore))
                .filter(group -> group.getPricePerHour() != 0.0)
                .collect(Collectors.toList());



        /*List<Group> result = getSheets(workbook)
                .stream()
                .map(sheet -> supplyAsync(() -> mapSheetToGroup(sheet, nextMonthDatesStore), executorService))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .filter(group -> group.getPricePerHour() != 0.0)
                .collect(Collectors.toList());

        executorService.shutdown();*/

        //return result;
    }

    private Group mapSheetToGroup(SheetInfo sheetInfo, NextMonthDatesStore nextMonthDatesStore) {
        Group group = new Group();
        CellAddress pricePerHourCell = new CellAddress(cellProps.getGroupInfoCells().getPricePerHour());
        CellAddress groupIdCell = new CellAddress(cellProps.getGroupInfoCells().getGroupId());
        CellAddress groupLevelCell = new CellAddress(cellProps.getGroupInfoCells().getGroupLevel());
        CellAddress teacherOneCell = new CellAddress(cellProps.getGroupInfoCells().getTeacherOne());
        CellAddress teacherTwoCell = new CellAddress(cellProps.getGroupInfoCells().getTeacherTwo());
        CellAddress classDurationOneCell = new CellAddress(cellProps.getGroupInfoCells().getClassDurationOne());
        CellAddress classDurationTwoCell = new CellAddress(cellProps.getGroupInfoCells().getClassDurationTwo());
        CellAddress classStartTimeCell = new CellAddress(cellProps.getGroupInfoCells().getClassStartTime());

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

    private Group mapSheetToGroup(Sheet sheet, NextMonthDatesStore nextMonthDatesStore) {
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

        List<CellAddress> cellAddressList = List.of(pricePerHourCell, groupIdCell, groupLevelCell, teacherOneCell,
                teacherTwoCell, classDurationOneCell, classDurationTwoCell, classStartTimeCell);
        Set<Integer> cellRows = cellAddressList
                .stream()
                .map(CellAddress::getRow)
                .collect(Collectors.toSet());
        group.setSheetName(sheet.getSheetName());

        Map<CellAddress, String> result = new HashMap<>();

        Map<DayOfWeek, CellAddress> daysOneMap = weekDaysMaps.weekDaysOneMap();
        Map<DayOfWeek, CellAddress> daysTwoMap = weekDaysMaps.weekDaysTwoMap();

        sheet.rowIterator().forEachRemaining(row -> {
            if (cellRows.contains(row.getRowNum())) {
                cellAddressList
                        .stream()
                        .filter(cellAddress -> cellAddress.getRow() == row.getRowNum())
                        .forEach(cellAddress -> {
                                    Optional<Cell> cellOptional = ofNullable(row.getCell(cellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                                    cellOptional.ifPresent(cell ->
                                            result.put(cellAddress, Utils.getCellValue(cell))
                                    );
                                }
                        );

                result.putAll(getDaysOneSchedule(daysOneMap, row));
                result.putAll(getDaysOneSchedule(daysTwoMap, row));

                Map<Integer, CellAddress> studentNames = studentInfoMaps.getStudentNames();

            }
        });

        if (result.containsKey(classStartTimeCell)) {
            String formatted = DateUtil.getLocalDateTime(Double.parseDouble(result.get(classStartTimeCell))).format(DateTimeFormatter.ofPattern("HH:mm"));
            result.put(classStartTimeCell, formatted);
        }
        System.out.println(result);
        /*getCell(sheet, pricePerHourCell)
                .ifPresent(cell -> group.setPricePerHour(toDoubleValue(cell)));
        if (group.getPricePerHour() > individualProps.getMinPrice()) {
            group.setIndividual(true);
        }
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
                .ifPresent(cell -> group.setClassStartTime(toStringValue(cell)));*/

        //initWeekDaysService.initWeekDays(sheet, group);
        /*List<Student> studentList = initStudentsService.initStudents(sheet);
        group.setStudentsInfo(studentList);*/

        //NextMonthDatesStore nextMonthDatesStore = buildNextMonthDatesStore();
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

    private Map<CellAddress, String> getDaysOneSchedule(Map<DayOfWeek, CellAddress> dayOfWeekCellAddressMap, Row row) {
        Map<CellAddress, String> result = new HashMap<>();
        List<CellAddress> daysOneCellAddressList = new ArrayList<>(dayOfWeekCellAddressMap.values());
        Set<Integer> daysOneRowNumSet = daysOneCellAddressList
                .stream()
                .map(CellAddress::getRow)
                .collect(Collectors.toSet());

        dayOfWeekCellAddressMap.forEach((dayOfWeek, cellAddress) -> {
            if (daysOneRowNumSet.contains(row.getRowNum())) {
                daysOneCellAddressList
                        .stream()
                        .filter(daysOneCellAddress -> daysOneCellAddress.getRow() == row.getRowNum())
                        .forEach(daysOneCellAddress -> {
                            Optional<Cell> cellOptional = ofNullable(row.getCell(daysOneCellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                            cellOptional.ifPresent(cell ->
                                    result.put(daysOneCellAddress, Utils.getCellValue(cell))
                            );
                        });
            }
        });

        return result;
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
