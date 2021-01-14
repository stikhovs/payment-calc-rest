package ru.payment.calc.payment_calculator.props;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static ru.payment.calc.payment_calculator.utils.Constants.*;

@Configuration
@RequiredArgsConstructor
public class GroupInfoMaps {

    private final CellProps cellProps;

    @Bean
    public Map<String, CellAddress> getGroupInfo() {
        CellAddress pricePerHourCell = new CellAddress(cellProps.getGroupInfoCells().getPricePerHour());
        CellAddress groupIdCell = new CellAddress(cellProps.getGroupInfoCells().getGroupId());
        CellAddress groupLevelCell = new CellAddress(cellProps.getGroupInfoCells().getGroupLevel());
        CellAddress teacherOneCell = new CellAddress(cellProps.getGroupInfoCells().getTeacherOne());
        CellAddress teacherTwoCell = new CellAddress(cellProps.getGroupInfoCells().getTeacherTwo());
        CellAddress classDurationOneCell = new CellAddress(cellProps.getGroupInfoCells().getClassDurationOne());
        CellAddress classDurationTwoCell = new CellAddress(cellProps.getGroupInfoCells().getClassDurationTwo());
        CellAddress classStartTimeCell = new CellAddress(cellProps.getGroupInfoCells().getClassStartTime());

        return Map.of(
                PRICE_PER_HOUR_STRING, pricePerHourCell,
                GROUP_ID_STRING, groupIdCell,
                GROUP_LEVEL_STRING, groupLevelCell,
                TEACHER_ONE_STRING, teacherOneCell,
                TEACHER_TWO_STRING, teacherTwoCell,
                CLASS_DURATION_ONE_STRING, classDurationOneCell,
                CLASS_DURATION_TWO_STRING, classDurationTwoCell,
                CLASS_START_TIME_STRING, classStartTimeCell
        );
    }
}
