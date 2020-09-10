package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.props.CellProps;
import ru.payment.calc.payment_calculator.service.InitGroupsService;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;

@Service
@RequiredArgsConstructor
public class InitGroupsServiceImpl implements InitGroupsService {

    private final CellProps cellProps;

    @Override
    public List<Group> init(XSSFWorkbook workbook) {
        return getSheets(workbook)
                .stream()
                .map(this::mapSheetToGroup)
                .filter(group -> group.getPricePerHour() != 0.0)
                .collect(Collectors.toList());
    }

    private List<XSSFSheet> getSheets(XSSFWorkbook workbook) {
        List<XSSFSheet> sheets = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetHidden(i)) {
                sheets.add(workbook.getSheetAt(i));
            }
        }
        return sheets;
    }

    private Group mapSheetToGroup(XSSFSheet sheet) {

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

        return group;
    }

    private Optional<XSSFCell> getCell(XSSFSheet sheet, CellAddress cellAddress) {
        if (sheet.getFirstRowNum() != -1) {
            return ofNullable(sheet.getRow(cellAddress.getRow()).getCell(cellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
        }
        return empty();
    }

    @SneakyThrows
    private double toDoubleValue(XSSFCell cell) {
        if (isParsable(cell.getRawValue())) {
            NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
            Number number = format.parse(getCellValue(cell));
            return number.doubleValue();
        }
        return 0.0;
    }

    private String toStringValue(XSSFCell cell) {
        return StringUtils.defaultIfBlank(getCellValue(cell), null);
    }

    private String toStringTimeValue(XSSFCell cell) {
        return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    private String getCellValue(XSSFCell cell) {
        DataFormatter df = new DataFormatter();
        XSSFFormulaEvaluator xssfFormulaEvaluator = new XSSFFormulaEvaluator(cell.getSheet().getWorkbook());
        return df.formatCellValue(cell, xssfFormulaEvaluator);
        /*CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getRawValue();
            default:
                return "";
        }*/
    }
}
