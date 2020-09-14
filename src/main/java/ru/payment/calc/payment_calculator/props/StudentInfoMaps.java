package ru.payment.calc.payment_calculator.props;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class StudentInfoMaps {

    private final CellProps cellProps;

    @Bean
    public Map<Integer, CellAddress> getStudentNames() {
        String start = cellProps.getStudentNames().getStart();
        String end = cellProps.getStudentNames().getEnd();
        return initCellMap(start, end);
    }

    @Bean
    public Map<Integer, CellAddress> getStudentBalance() {
        String start = cellProps.getStudentBalance().getStart();
        String end = cellProps.getStudentBalance().getEnd();
        return initCellMap(start, end);
    }

    private HashMap<Integer, CellAddress> initCellMap(String start, String end) {
        HashMap<Integer, CellAddress> result = new HashMap<>();
        CellAddress firstStudentCellAddress = new CellAddress(start);
        CellAddress lastStudentCellAddress = new CellAddress(end);
        for (int i = 0; i < lastStudentCellAddress.getRow(); i++) {
            CellAddress cellAddress = new CellAddress(firstStudentCellAddress.getRow() + i, firstStudentCellAddress.getColumn());
            result.put(cellAddress.getRow(), cellAddress);
        }
        return result;
    }

}
