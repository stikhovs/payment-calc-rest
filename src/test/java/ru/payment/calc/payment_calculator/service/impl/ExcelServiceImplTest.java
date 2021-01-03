package ru.payment.calc.payment_calculator.service.impl;


import org.junit.jupiter.api.Test;
import ru.payment.calc.payment_calculator.model.ExcelSheetEnum;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExcelServiceImplTest {

    @Test
    public void test() {
        List<String> collect = Stream.of(ExcelSheetEnum.values())
                .map(ExcelSheetEnum::getName)
                .collect(Collectors.toList());
        System.out.println(collect);
    }

}
