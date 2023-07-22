package ru.payment.calc.payment_calculator.service;

import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;

import java.math.BigDecimal;

public interface NextMonthHoursService {

    BigDecimal calcNextMonthHours(Group group, NextMonthDatesStore nextMonthDatesStore);

}
