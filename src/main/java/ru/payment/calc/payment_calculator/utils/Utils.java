package ru.payment.calc.payment_calculator.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class Utils {

    public static <T> void nonNullSet(T setCandidate, Consumer<T> ifNonNullCandidateConsumer) {
        Optional.ofNullable(setCandidate).ifPresent(ifNonNullCandidateConsumer);
    }

    public static <T> T nonNullGet(Object getCandidate, Supplier<T> ifNonNullCandidateSupplier) {
        return Optional.ofNullable(getCandidate).isPresent() ? ifNonNullCandidateSupplier.get() : null;
    }

    public static Optional<XSSFCell> getCell(XSSFSheet sheet, CellAddress cellAddress) {
        if (sheet.getFirstRowNum() != -1 && cellAddress != null) {
            return ofNullable(sheet.getRow(cellAddress.getRow()).getCell(cellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
        }
        return empty();
    }

    public static double toDoubleValue(XSSFCell cell) {
        String cellValue = getCellValue(cell);
        return parseStringToDouble(cellValue);
    }

    @SneakyThrows
    public static double parseStringToDouble(String cellValue) {
        if (cellValue.trim().matches("-?\\d+,?\\d*")) {
            NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
            Number number = format.parse(cellValue);
            return number.doubleValue();
        }
        return 0.0;
    }

    public static String toStringValue(XSSFCell cell) {
        return StringUtils.defaultIfBlank(getCellValue(cell), null);
    }

    public static boolean toBooleanValue(XSSFCell cell) {
        return cell.getBooleanCellValue();
    }

    public static String getCellValue(XSSFCell cell) {
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
