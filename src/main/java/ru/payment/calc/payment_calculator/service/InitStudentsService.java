package ru.payment.calc.payment_calculator.service;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.payment.calc.payment_calculator.model.Student;

import java.util.List;

public interface InitStudentsService {

    List<Student> initStudents(XSSFSheet sheet);

}
