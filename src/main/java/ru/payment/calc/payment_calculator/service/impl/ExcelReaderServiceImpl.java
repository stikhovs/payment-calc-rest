package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.SheetInfo;
import ru.payment.calc.payment_calculator.props.GroupInfoMaps;
import ru.payment.calc.payment_calculator.props.StudentInfoMaps;
import ru.payment.calc.payment_calculator.props.WeekDaysMaps;
import ru.payment.calc.payment_calculator.service.ExcelReaderService;
import ru.payment.calc.payment_calculator.utils.Utils;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.payment.calc.payment_calculator.utils.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelReaderServiceImpl implements ExcelReaderService {

    private final GroupInfoMaps groupInfoMaps;
    private final WeekDaysMaps weekDaysMaps;
    private final StudentInfoMaps studentInfoMaps;

    @Override
    public List<SheetInfo> readWorkbook(Workbook workbook) {
        return getSheets(workbook)
                .stream()
                .map(this::readSheet)
                .collect(Collectors.toList());
    }

    private SheetInfo readSheet(Sheet sheet) {
        SheetInfo sheetInfo = new SheetInfo();
        sheetInfo.setSheetName(sheet.getSheetName());
        Map<CellAddress, String> sheetInfoMap = new HashMap<>();
        sheet.rowIterator().forEachRemaining(row -> {
            sheetInfoMap.putAll(readGroupInfo(row, groupInfoMaps.getGroupInfo()));

            sheetInfoMap.putAll(readDaysSchedule(row, weekDaysMaps.weekDaysOneMap()));
            sheetInfoMap.putAll(readDaysSchedule(row, weekDaysMaps.weekDaysTwoMap()));

            sheetInfoMap.putAll(readStudentsInfo(row, studentInfoMaps.getStudentNames()));
            sheetInfoMap.putAll(readStudentsInfo(row, studentInfoMaps.getStudentBalance()));
            sheetInfoMap.putAll(readStudentsInfo(row, studentInfoMaps.getIndividualGraphic()));
            sheetInfoMap.putAll(readStudentsInfo(row, studentInfoMaps.getPermanentDiscount()));
            sheetInfoMap.putAll(readStudentsInfo(row, studentInfoMaps.getSingleDiscount()));
        });
        sheetInfo.setSheetData(sheetInfoMap);

        validateSheet(sheetInfo);

        return sheetInfo;
    }

    private Map<CellAddress, String> readGroupInfo(Row row, Map<String, CellAddress> groupInfo) {
        Map<CellAddress, String> result = new HashMap<>();

        ArrayList<CellAddress> cellAddresses = new ArrayList<>(groupInfo.values());
        Set<Integer> rowSet = cellAddresses
                .stream()
                .map(CellAddress::getRow)
                .collect(Collectors.toSet());

        cellAddresses.forEach(cellAddress -> {
            if (rowSet.contains(row.getRowNum())) {
                cellAddresses
                        .stream()
                        .filter(groupInfoCellAddress -> groupInfoCellAddress.getRow() == row.getRowNum())
                        .forEach(groupInfoCellAddress -> {
                            Optional<Cell> cellOptional = ofNullable(row.getCell(groupInfoCellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                            cellOptional.ifPresent(cell -> {
                                        String cellValue = Utils.getCellValue(cell);
                                        if (StringUtils.isNotBlank(cellValue)) {
                                            result.put(groupInfoCellAddress, cellValue);
                                        }
                                    }
                            );
                        });
            }
        });

        if (result.containsKey(groupInfo.get(CLASS_START_TIME_STRING))) {
            CellAddress classStartTimeCell = groupInfo.get(CLASS_START_TIME_STRING);
            String classStartTimeStr = result.get(classStartTimeCell);
            if (StringUtils.isNotBlank(classStartTimeStr) && NumberUtils.isParsable(classStartTimeStr)) {
                String formatted = DateUtil.getLocalDateTime(Double.parseDouble(classStartTimeStr)).format(DateTimeFormatter.ofPattern("HH:mm"));
                result.put(classStartTimeCell, formatted);
            }
        }

        return result;
    }

    private Map<CellAddress, String> readDaysSchedule(Row row, Map<DayOfWeek, CellAddress> dayOfWeekCellAddressMap) {
        Map<CellAddress, String> result = new HashMap<>();
        List<CellAddress> daysCellAddressList = new ArrayList<>(dayOfWeekCellAddressMap.values());
        Set<Integer> daysRowNumSet = daysCellAddressList
                .stream()
                .map(CellAddress::getRow)
                .collect(Collectors.toSet());

        dayOfWeekCellAddressMap.forEach((dayOfWeek, cellAddress) -> {
            if (daysRowNumSet.contains(row.getRowNum())) {
                daysCellAddressList
                        .stream()
                        .filter(dayCellAddress -> dayCellAddress.getRow() == row.getRowNum())
                        .forEach(dayCellAddress -> {
                            Optional<Cell> cellOptional = ofNullable(row.getCell(dayCellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                            cellOptional.ifPresent(cell ->
                                    result.put(dayCellAddress, Utils.getCellValue(cell))
                            );
                        });
            }
        });

        return result;
    }

    private Map<CellAddress, String> readStudentsInfo(Row row, Map<Integer, CellAddress> studentInfoMap) {
        Map<CellAddress, String> result = new HashMap<>();

        ArrayList<CellAddress> cellAddressList = new ArrayList<>(studentInfoMap.values());
        Set<Integer> rowSet = studentInfoMap.keySet();

        studentInfoMap.forEach((rowNum, cellAddress) -> {
            if (rowSet.contains(row.getRowNum())) {
                cellAddressList
                        .stream()
                        .filter(groupInfoCellAddress -> groupInfoCellAddress.getRow() == row.getRowNum())
                        .forEach(groupInfoCellAddress -> {
                            Optional<Cell> cellOptional = ofNullable(row.getCell(groupInfoCellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                            cellOptional.ifPresent(cell ->
                                    result.put(groupInfoCellAddress, Utils.getCellValue(cell))
                            );
                        });
            }
        });

        return result;
    }

    private void validateSheet(SheetInfo sheetInfo) {
        Map<String, CellAddress> groupInfo = groupInfoMaps.getGroupInfo();
        boolean hasPricePerHour = sheetInfo.getSheetData().containsKey(groupInfo.get(PRICE_PER_HOUR_STRING));
        boolean hasGroupId = sheetInfo.getSheetData().containsKey(groupInfo.get(GROUP_ID_STRING));
        boolean hasGroupLevel = sheetInfo.getSheetData().containsKey(groupInfo.get(GROUP_LEVEL_STRING));
        boolean hasTeacherOne = sheetInfo.getSheetData().containsKey(groupInfo.get(TEACHER_ONE_STRING));
        boolean hasTeacherTwo = sheetInfo.getSheetData().containsKey(groupInfo.get(TEACHER_TWO_STRING));
        boolean hasClassDurationOne = sheetInfo.getSheetData().containsKey(groupInfo.get(CLASS_DURATION_ONE_STRING));
        boolean hasClassDurationTwo = sheetInfo.getSheetData().containsKey(groupInfo.get(CLASS_DURATION_TWO_STRING));
        boolean hasClassStartTime = sheetInfo.getSheetData().containsKey(groupInfo.get(CLASS_START_TIME_STRING));

        if (hasPricePerHour && hasGroupId && hasGroupLevel && (hasTeacherOne || hasTeacherTwo) && (hasClassDurationOne || hasClassDurationTwo) && hasClassStartTime) {
            sheetInfo.setValid(true);
        }
    }

    private List<Sheet> getSheets(Workbook workbook) {
        log.info("Start reading sheets");
        List<Sheet> sheets = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetHidden(i)) {
                sheets.add(workbook.getSheetAt(i));
            }
        }
        return sheets;
    }
}
