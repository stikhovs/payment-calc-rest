package ru.payment.calc.payment_calculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.payment.calc.payment_calculator.controller.dto.request.StudentRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.StudentResponse;
import ru.payment.calc.payment_calculator.model.Student;

import java.math.BigDecimal;
import java.util.Optional;


@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "discount", source = "studentRequest", qualifiedByName = "getDiscount")
    @Mapping(target = "hasDebt", source = "studentRequest", qualifiedByName = "defineDebt")
    Student toStudent(StudentRequest studentRequest);

    StudentResponse toStudentResponse(Student student);

    Student toStudent(StudentResponse student);

    @Named("getDiscount")
    default double getDiscount(StudentRequest studentRequest) {
        return handleDiscount(studentRequest.getSingleDiscount())
                .orElseGet(() ->
                        handleDiscount(studentRequest.getPermanentDiscount())
                                .orElse(0.0));

    }

    @Named("defineDebt")
    default boolean defineDebt(StudentRequest studentRequest) {
        return studentRequest.getBalance().compareTo(BigDecimal.valueOf(-0.03)) < 0;
    }

    private Optional<Double> handleDiscount(BigDecimal discount) {
        if (discount == null) {
            return Optional.empty();
        }
        return Optional.of(discount.doubleValue());
    }
}
