package ru.payment.calc.payment_calculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupRequest;
import ru.payment.calc.payment_calculator.controller.dto.response.GroupResponse;
import ru.payment.calc.payment_calculator.model.Group;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = StudentMapper.class)
public interface GroupMapper {

    @Mapping(target = "sheetName", source = "groupName")
    @Mapping(target = "studentsInfo", source = "students")
    @Mapping(target = "individual", source = "request", qualifiedByName = "defineIndividual")
    Group toGroup(GroupRequest request);

    @Mapping(target = "groupName", source = "sheetName")
    @Mapping(target = "students", source = "studentsInfo")
    GroupResponse toGroupResponse(Group group);

    @Mapping(target = "sheetName", source = "groupName")
    @Mapping(target = "studentsInfo", source = "students")
    Group toGroup(GroupResponse group);

    @Named("defineIndividual")
    default boolean defineIndividual(GroupRequest request) {
        return request.getPricePerHour().compareTo(BigDecimal.valueOf(1500)) > 0;
    }


}
