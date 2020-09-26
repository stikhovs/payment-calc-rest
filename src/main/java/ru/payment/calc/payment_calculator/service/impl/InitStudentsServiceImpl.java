package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Student;
import ru.payment.calc.payment_calculator.props.StudentInfoMaps;
import ru.payment.calc.payment_calculator.service.InitStudentsService;
import ru.payment.calc.payment_calculator.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.remove;
import static ru.payment.calc.payment_calculator.utils.Utils.getCell;
import static ru.payment.calc.payment_calculator.utils.Utils.toStringValue;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitStudentsServiceImpl implements InitStudentsService {

    private final StudentInfoMaps studentInfoMaps;

    @Override
    public List<Student> initStudents(XSSFSheet sheet) {
        return studentInfoMaps
                .getStudentNames()
                .keySet()
                .stream()
                .map(row -> buildStudent(sheet, row))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Student> buildStudent(XSSFSheet sheet, Integer row) {
        CellAddress nameCell = studentInfoMaps.getStudentNames().get(row);
        CellAddress balanceCell = studentInfoMaps.getStudentBalance().get(row);
        CellAddress individualGraphicCell = studentInfoMaps.getIndividualGraphic().get(row);
        CellAddress singleDiscountCell = studentInfoMaps.getSingleDiscount().get(row);
        CellAddress permanentDiscountCell = studentInfoMaps.getPermanentDiscount().get(row);
        String studentName = getStudentName(sheet, nameCell);
        if (isNotBlank(studentName)) {
            return of(Student.builder()
                    .name(studentName)
                    .balance(getStudentBalance(sheet, balanceCell))
                    .indGraphic(getIndividualGraphic(sheet, individualGraphicCell))
                    .discount(getDiscount(sheet, singleDiscountCell, permanentDiscountCell))
                    .build());
        } else {
            return empty();
        }
    }

    private String getStudentName(XSSFSheet sheet, CellAddress cellAddress) {
        StringBuilder studentName = new StringBuilder();
        getCell(sheet, cellAddress)
                .ifPresent(cell -> studentName.append(toStringValue(cell)));
        return studentName.toString();
    }

    private Double getStudentBalance(XSSFSheet sheet, CellAddress cellAddress) {
        return getCell(sheet, cellAddress)
                .stream()
                .map(Utils::toDoubleValue)
                .findFirst()
                .orElse(0.0);
    }

    private boolean getIndividualGraphic(XSSFSheet sheet, CellAddress cellAddress) {
        return getCell(sheet, cellAddress)
                .stream()
                .map(Utils::toStringValue)
                .map(StringUtils::isNotBlank)
                .findFirst()
                .orElse(false);
    }

    private double getDiscount(XSSFSheet sheet, CellAddress singleDiscount, CellAddress permanentDiscount) {
        return handleDiscount(sheet, singleDiscount)
                .orElseGet(() ->
                        handleDiscount(sheet, permanentDiscount)
                                .orElse(0.0));

    }

    private Optional<Double> handleDiscount(XSSFSheet sheet, CellAddress discountCell) {
        return getCell(sheet, discountCell)
                .map(Utils::toStringValue)
                .map(discount -> remove(discount.trim(), "%"))
                .map(Utils::parseStringToDouble);
    }
}
