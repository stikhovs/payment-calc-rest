package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.ExcelSheetEnum;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.Student;
import ru.payment.calc.payment_calculator.props.DebtThreshold;
import ru.payment.calc.payment_calculator.service.ExcelService;
import ru.payment.calc.payment_calculator.service.GroupDividerService;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final GroupDividerService groupDividerService;

    private final DebtThreshold debtThreshold;

    @Override
    public XSSFWorkbook createExcel(List<Group> groups, String month) {
        XSSFWorkbook workbook = initWorkbook();

        Map<ExcelSheetEnum, List<Group>> groupsBySheets = groupDividerService.divideGroupsBySheets(groups);

        groupsBySheets.forEach((excelSheetEnum, groupList) -> {
            XSSFSheet sheet = workbook.getSheet(excelSheetEnum.getName());
            AtomicInteger rowNum = new AtomicInteger(0);
            createSheetHeader(workbook, sheet, rowNum, month);
            groupList.forEach(group -> {
                createGroupHeader(workbook, sheet, rowNum, group);
                createGroupSubHeader(workbook, sheet, rowNum);
                AtomicInteger studentNumber = new AtomicInteger(1);
                group.getStudentsInfo().forEach(student ->
                        createStudentInfo(workbook, sheet, rowNum, student, studentNumber)
                );
                setSpaceBetweenGroups(sheet, rowNum);
            });
            setColumnAutosize(sheet);
        });

        return workbook;
    }

    private void createSheetHeader(XSSFWorkbook workbook, XSSFSheet sheet, AtomicInteger rowNum, String month) {
        XSSFRow row = sheet.createRow(rowNum.getAndAdd(1));
        XSSFCell cell = row.createCell(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        cell.setCellValue(sheet.getSheetName() + " " + month);

        XSSFCellStyle headerCellStyle = createHeaderCellStyle(workbook);
        cell.setCellStyle(headerCellStyle);
    }

    private void createGroupHeader(XSSFWorkbook workbook, XSSFSheet sheet, AtomicInteger rowNum, Group group) {
        int rowIndex = rowNum.getAndAdd(1);
        XSSFRow row = sheet.createRow(rowIndex);
        XSSFCell cell = row.createCell(0);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 4));
        RegionUtil.setBorderBottom(BorderStyle.THIN, new CellRangeAddress(rowIndex, rowIndex, 0, 4), sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, new CellRangeAddress(rowIndex, rowIndex, 0, 4), sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, new CellRangeAddress(rowIndex, rowIndex, 0, 4), sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, new CellRangeAddress(rowIndex, rowIndex, 0, 4), sheet);
        cell.setCellValue(
                group.getGroupId()
                + " (" + group.getSheetName() + "), " + (int) group.getPricePerHour() + " р/ач "
                + ", Начало: " + group.getClassStartTime() + ", " + getScheduleDays(group)
                + "\n" + getTeachers(group) + ", " + group.getGroupLevel()
                + ", Длительность: " + getDuration(group) + " а/ч" + ", "
                + "\n" + "Часов в следующем месяце: " + String.format("%.2f", group.getNextMonthHours()));
        cell.getRow().setHeight((short) (cell.getRow().getHeight() * 3));

        XSSFCellStyle groupInfoCellStyle = createGroupInfoCellStyle(workbook);
        cell.setCellStyle(groupInfoCellStyle);

        XSSFCell leftBorderCell = row.createCell(5);
        XSSFCellStyle leftBorderCellStyle = createLeftBorderCellStyle(workbook);
        leftBorderCell.setCellStyle(leftBorderCellStyle);
    }

    private String getDuration(Group group) {
        String classDurationOne = null;
        String classDurationTwo = null;
        if (group.getClassDurationOne() != 0.0) {
            classDurationOne = String.format("%.2f", group.getClassDurationOne());
        }
        if (group.getClassDurationTwo() != 0.0) {
            classDurationTwo = String.format("%.2f", group.getClassDurationTwo());
        }

        return Stream.concat(Stream.ofNullable(classDurationOne), Stream.ofNullable(classDurationTwo))
                .distinct()
                .reduce((duration1, duration2) -> duration1 + ", " + duration2)
                .orElse("NONE");
    }

    private String getTeachers(Group group) {
        String teacherOne = group.getTeacherOne();
        String teacherTwo = group.getTeacherTwo();

        return Stream.concat(Stream.ofNullable(teacherOne), Stream.ofNullable(teacherTwo))
                .distinct()
                .reduce((teacher1, teacher2) -> teacher1 + ", " + teacher2)
                .orElse("NONE");
    }

    private String getScheduleDays(Group group) {
        List<DayOfWeek> classDaysOne = group.getClassDaysOne();
        List<DayOfWeek> classDaysTwo = group.getClassDaysTwo();
        return CollectionUtils
                .union(classDaysOne, classDaysTwo)
                .stream()
                .distinct()
                .sorted()
                .map(dayOfWeek -> dayOfWeek.getDisplayName(TextStyle.SHORT, new Locale("ru")))
                .reduce((s, s2) -> s + ", " + s2)
                .orElse("NONE");
    }

    private void createGroupSubHeader(XSSFWorkbook workbook, XSSFSheet sheet, AtomicInteger rowNum) {
        int rowIndex = rowNum.getAndAdd(1);
        XSSFRow row = sheet.createRow(rowIndex);
        AtomicInteger cellIndex = new AtomicInteger(0);
        XSSFCell cell = row.createCell(cellIndex.getAndAdd(1));
        XSSFCellStyle groupHeaderCellStyle = createGroupHeaderCellStyle(workbook);
        cell.setCellValue("№");
        cell.setCellStyle(groupHeaderCellStyle);
        cell = row.createCell(cellIndex.getAndAdd(1));
        cell.setCellValue("ФИО студента");
        cell.setCellStyle(groupHeaderCellStyle);
        cell = row.createCell(cellIndex.getAndAdd(1));
        cell.setCellValue("Скидка");
        cell.setCellStyle(groupHeaderCellStyle);
        cell = row.createCell(cellIndex.getAndAdd(1));
        cell.setCellValue("Часов \nк оплате");
        cell.setCellStyle(groupHeaderCellStyle);
        cell = row.createCell(cellIndex.get());
        cell.setCellValue("Оплата \n в руб");
        cell.setCellStyle(groupHeaderCellStyle);
    }

    private void createStudentInfo(XSSFWorkbook workbook, XSSFSheet sheet, AtomicInteger rowNum, Student student, AtomicInteger studentNumber) {
        int rowIndex = rowNum.getAndAdd(1);
        XSSFRow row = sheet.createRow(rowIndex);
        AtomicInteger cellIndex = new AtomicInteger(0);
        XSSFCellStyle leftBorderCellStyle = createLeftBorderCellStyle(workbook);
        XSSFCellStyle centerCellStyle = createCenterCellStyle(workbook);

        XSSFCell studentNumberCell = row.createCell(cellIndex.getAndAdd(1));
        studentNumberCell.setCellValue(studentNumber.getAndAdd(1) + ")");
        studentNumberCell.setCellStyle(leftBorderCellStyle);

        XSSFCell studentNameCell = row.createCell(cellIndex.getAndAdd(1));
        if (student.isIndGraphic()) {
            studentNameCell.setCellValue(student.getName() + " (инд график!)");
        } else {
            studentNameCell.setCellValue(student.getName());
        }


        XSSFCell discountCell = row.createCell(cellIndex.getAndAdd(1));
        if (student.getDiscount() != 0.0) {
            discountCell.setCellValue((int) student.getDiscount() + "%");
        }
        discountCell.setCellStyle(centerCellStyle);

        XSSFCell payHourCell = row.createCell(cellIndex.getAndAdd(1));
        if (student.getHoursToPay() <= debtThreshold.getThreshold()) {
            payHourCell.setCellValue("не должен");
        } else {
            payHourCell.setCellValue(student.getHoursToPay());
        }
        XSSFCellStyle payHourCellStyle = createPayHourCellStyle(workbook);
        payHourCell.setCellStyle(payHourCellStyle);

        XSSFCell payMoneyCell = row.createCell(cellIndex.getAndAdd(1));
        if (student.getMoneyToPay() <= debtThreshold.getThreshold()) {
            payMoneyCell.setCellValue("");
        } else {
            payMoneyCell.setCellValue(String.format("%.2f", student.getMoneyToPay()) + " руб");
        }
        XSSFCellStyle payMoneyCellStyle = createPayMoneyCellStyle(workbook);
        payMoneyCell.setCellStyle(payMoneyCellStyle);

        XSSFCell leftBorderCell = row.createCell(cellIndex.get());
        leftBorderCell.setCellStyle(leftBorderCellStyle);

        RegionUtil.setBorderBottom(BorderStyle.DASHED, new CellRangeAddress(rowIndex, rowIndex, 0, 4), sheet);
    }

    private void setSpaceBetweenGroups(XSSFSheet sheet, AtomicInteger rowNum) {
        int rowIndex = rowNum.getAndAdd(1);
        XSSFRow row = sheet.createRow(rowIndex);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 4));
        RegionUtil.setBorderTop(BorderStyle.THIN, new CellRangeAddress(rowIndex, rowIndex, 0, 4), sheet);
        row.setHeight((short) (row.getHeight() * 0.5));
    }

    private void setColumnAutosize(XSSFSheet sheet) {
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private XSSFWorkbook initWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Stream.of(ExcelSheetEnum.values())
                .map(ExcelSheetEnum::getName)
                .forEach(workbook::createSheet);
        return workbook;
    }

    private XSSFCellStyle createGroupInfoCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle groupInfoCellStyle = workbook.createCellStyle();
        groupInfoCellStyle.setWrapText(true);
        groupInfoCellStyle.setAlignment(HorizontalAlignment.CENTER);
        groupInfoCellStyle.setBorderTop(BorderStyle.THIN);
        groupInfoCellStyle.setBorderBottom(BorderStyle.THIN);
        groupInfoCellStyle.setBorderLeft(BorderStyle.THIN);
        return groupInfoCellStyle;
    }

    private XSSFCellStyle createPayHourCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle payHourCellStyle = workbook.createCellStyle();
        payHourCellStyle.setDataFormat((short) 2);
        payHourCellStyle.setAlignment(HorizontalAlignment.CENTER);
        return payHourCellStyle;
    }

    private XSSFCellStyle createCenterCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle centerCellStyle = workbook.createCellStyle();
        centerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        return centerCellStyle;
    }

    private XSSFCellStyle createHeaderCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        Font sheetHeaderFont = workbook.createFont();
        sheetHeaderFont.setBold(true);
        sheetHeaderFont.setFontHeightInPoints((short) 16);
        headerCellStyle.setFont(sheetHeaderFont);
        return headerCellStyle;
    }

    private XSSFCellStyle createGroupHeaderCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle groupHeaderCellStyle = workbook.createCellStyle();
        groupHeaderCellStyle.setAlignment(HorizontalAlignment.CENTER);
        groupHeaderCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        groupHeaderCellStyle.setBorderRight(BorderStyle.THIN);
        groupHeaderCellStyle.setBorderBottom(BorderStyle.THIN);
        groupHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
        groupHeaderCellStyle.setAlignment(HorizontalAlignment.CENTER);
        groupHeaderCellStyle.setWrapText(true);
        Font subHeaderFont = workbook.createFont();
        subHeaderFont.setBold(true);
        groupHeaderCellStyle.setFont(subHeaderFont);
        return groupHeaderCellStyle;
    }

    private XSSFCellStyle createRightBorderCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle rightBorderCellStyle = workbook.createCellStyle();
        rightBorderCellStyle.setBorderRight(BorderStyle.THIN);
        return rightBorderCellStyle;
    }

    private XSSFCellStyle createLeftBorderCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle leftBorderCellStyle = workbook.createCellStyle();
        leftBorderCellStyle.setBorderLeft(BorderStyle.THIN);
        return leftBorderCellStyle;
    }

    private XSSFCellStyle createPayMoneyCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle payMoneyCellStyle = workbook.createCellStyle();
        payMoneyCellStyle.setAlignment(HorizontalAlignment.CENTER);
        payMoneyCellStyle.setBorderRight(BorderStyle.THIN);
        return payMoneyCellStyle;
    }

}
