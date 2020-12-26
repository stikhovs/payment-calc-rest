package ru.payment.calc.payment_calculator.service;

import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;

public interface NextMonthHoursService {

    double calcNextMonthHours(Group group, NextMonthDatesStore nextMonthDatesStore);

}
