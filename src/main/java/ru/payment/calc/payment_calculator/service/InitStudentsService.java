package ru.payment.calc.payment_calculator.service;

import org.apache.poi.ss.util.CellAddress;
import ru.payment.calc.payment_calculator.model.Student;

import java.util.List;
import java.util.Map;

public interface InitStudentsService {

    List<Student> initStudents(Map<CellAddress, String> sheetData);

}
