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

    @Bean
    public Map<Integer, CellAddress> getIndividualGraphic() {
        String start = cellProps.getIndividualGraphic().getStart();
        String end = cellProps.getIndividualGraphic().getEnd();
        return initCellMap(start, end);
    }

    @Bean
    public Map<Integer, CellAddress> getSingleDiscount() {
        String start = cellProps.getSingleDiscount().getStart();
        String end = cellProps.getSingleDiscount().getEnd();
        return initCellMap(start, end);
    }

    @Bean
    public Map<Integer, CellAddress> getPermanentDiscount() {
        String start = cellProps.getPermanentDiscount().getStart();
        String end = cellProps.getPermanentDiscount().getEnd();
        return initCellMap(start, end);
    }

    private HashMap<Integer, CellAddress> initCellMap(String start, String end) {
        HashMap<Integer, CellAddress> result = new HashMap<>();
        CellAddress firstStudentCellAddress = new CellAddress(start);
        CellAddress lastStudentCellAddress = new CellAddress(end);
        for (int i = firstStudentCellAddress.getRow(); i < lastStudentCellAddress.getRow(); i++) {
            CellAddress cellAddress = new CellAddress(i, firstStudentCellAddress.getColumn());
            result.put(cellAddress.getRow(), cellAddress);
        }
        return result;
    }

}
