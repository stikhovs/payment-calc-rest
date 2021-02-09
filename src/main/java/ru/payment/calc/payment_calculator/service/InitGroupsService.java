package ru.payment.calc.payment_calculator.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import ru.payment.calc.payment_calculator.model.Group;
import ru.payment.calc.payment_calculator.model.NextMonthDatesStore;

public interface InitGroupsService {

    Flux<ServerSentEvent<Group>> init(Workbook workbook, NextMonthDatesStore nextMonthDatesStore);

}
