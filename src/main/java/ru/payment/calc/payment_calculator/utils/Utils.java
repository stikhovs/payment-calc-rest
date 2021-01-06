package ru.payment.calc.payment_calculator.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.poi.ss.usermodel.*;
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

    public static Optional<Cell> getCell(Sheet sheet, CellAddress cellAddress) {
        if (sheet.getSheetName().equals("12m")) {
            System.out.println("Sheet: " + sheet.getSheetName());
        }
        if (cellAddress != null) {
            for (Row row : sheet) {
                if (row.getRowNum() == cellAddress.getRow()) {
                    return ofNullable(row.getCell(cellAddress.getColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                }
            }
        }
        return empty();
    }

    public static double toDoubleValue(Cell cell) {
        String cellValue = getCellValue(cell);
        return parseStringToDouble(cellValue);
    }

    @SneakyThrows
    public static double parseStringToDouble(String cellValue) {
        if (NumberUtils.isParsable(cellValue)) {
            return Precision.round(Double.parseDouble(cellValue), 2);
        }
        return 0.0;
        /*if (cellValue.trim().matches("-?\\d+\\.?\\d*")) {
            NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
            Number number = format.parse(cellValue);
            return number.doubleValue();
        }*/
    }

    public static String toStringValue(Cell cell) {
        return StringUtils.defaultIfBlank(getCellValue(cell), null);
    }

    public static boolean toBooleanValue(Cell cell) {
        return cell.getBooleanCellValue();
    }

    public static String getCellValue(Cell cell) {
        /*DataFormatter df = new DataFormatter();
        XSSFFormulaEvaluator xssfFormulaEvaluator = new XSSFFormulaEvaluator(cell.getSheet().getWorkbook());
        return df.formatCellValue(cell, xssfFormulaEvaluator);*/
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: {
                CellType cachedFormulaResultType = cell.getCachedFormulaResultType();
                switch (cachedFormulaResultType) {
                    case STRING: return cell.getStringCellValue();
                    case NUMERIC: return String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
                    default:
                        return "";
                }
            }
            default:
                return "";
        }
    }

}
