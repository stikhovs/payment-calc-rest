package ru.payment.calc.payment_calculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.payment.calc.payment_calculator.controller.dto.request.StudentRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.StudentResponse;
import ru.payment.calc.payment_calculator.model.Student;

import java.math.BigDecimal;

import static java.util.Optional.ofNullable;


@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "discount", source = "studentRequest", qualifiedByName = "getDiscount")
    @Mapping(target = "hasDebt", source = "studentRequest", qualifiedByName = "defineDebt")
    Student toStudent(StudentRequest studentRequest);

    StudentResponse toStudentResponse(Student student);

    @Named("getDiscount")
    default BigDecimal getDiscount(StudentRequest studentRequest) {
        return ofNullable(studentRequest.getSingleDiscount())
                .orElseGet(() ->
                        ofNullable(studentRequest.getPermanentDiscount())
                                .orElse(BigDecimal.ZERO));

    }

    @Named("defineDebt")
    default boolean defineDebt(StudentRequest studentRequest) {
        return studentRequest.getBalance().compareTo(BigDecimal.valueOf(-0.03)) < 0;
    }

}
