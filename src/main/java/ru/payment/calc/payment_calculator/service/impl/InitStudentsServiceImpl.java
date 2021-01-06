package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.model.Student;
import ru.payment.calc.payment_calculator.props.DebtThreshold;
import ru.payment.calc.payment_calculator.props.StudentInfoMaps;
import ru.payment.calc.payment_calculator.service.InitStudentsService;
import ru.payment.calc.payment_calculator.utils.Utils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.parseBoolean;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.remove;
import static ru.payment.calc.payment_calculator.utils.Utils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitStudentsServiceImpl implements InitStudentsService {

    private final StudentInfoMaps studentInfoMaps;

    private final DebtThreshold debtThreshold;

    @Override
    public List<Student> initStudents(Map<CellAddress, String> sheetData) {

        return studentInfoMaps
                .getStudentNames()
                .values()
                .stream()
                .map(CellAddress::getRow)
                .map(rowNum -> {
                    CellAddress nameCell = studentInfoMaps.getStudentNames().get(rowNum);
                    CellAddress balanceCell = studentInfoMaps.getStudentBalance().get(rowNum);
                    CellAddress individualGraphicCell = studentInfoMaps.getIndividualGraphic().get(rowNum);
                    CellAddress singleDiscountCell = studentInfoMaps.getSingleDiscount().get(rowNum);
                    CellAddress permanentDiscountCell = studentInfoMaps.getPermanentDiscount().get(rowNum);

                    Student student = new Student();
                    nonNullSet(sheetData.get(nameCell), student::setName);
                    nonNullSet(sheetData.get(balanceCell), balance ->
                        student.setBalance(Utils.parseStringToDouble(balance))
                    );
                    nonNullSet(sheetData.get(individualGraphicCell), indGraphic -> student.setIndGraphic(parseBoolean(indGraphic)));
                    student.setDiscount(getDiscount(sheetData.get(singleDiscountCell), sheetData.get(permanentDiscountCell)));

                    return student;

                })
                .filter(student -> isNotBlank(student.getName()))
                .peek(this::defineDebt)
                .collect(Collectors.toList());
    }


    private String getStudentName(Sheet sheet, CellAddress cellAddress) {
        StringBuilder studentName = new StringBuilder();
        getCell(sheet, cellAddress)
                .ifPresent(cell -> studentName.append(toStringValue(cell)));
        return studentName.toString();
    }

    private Double getStudentBalance(Sheet sheet, CellAddress cellAddress) {
        return getCell(sheet, cellAddress)
                .stream()
                .map(Utils::toDoubleValue)
                .findFirst()
                .orElse(0.0);
    }

    private boolean getIndividualGraphic(Sheet sheet, CellAddress cellAddress) {
        return getCell(sheet, cellAddress)
                .stream()
                .map(Utils::toStringValue)
                .map(StringUtils::isNotBlank)
                .findFirst()
                .orElse(false);
    }

    private double getDiscount(String singleDiscount, String permanentDiscount) {
        return handleDiscount(singleDiscount)
                .orElseGet(() ->
                        handleDiscount(permanentDiscount)
                                .orElse(0.0));

    }

    private Optional<Double> handleDiscount(String discountCell) {
        if (discountCell == null) {
            return Optional.empty();
        }
        return Optional.of(parseStringToDouble(discountCell) * 100.0);
    }

    private void defineDebt(Student student) {
        if (student.getBalance() < debtThreshold.getThreshold()) {
            student.setHasDebt(true);
        }
    }
}
