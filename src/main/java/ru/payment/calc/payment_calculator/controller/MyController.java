package ru.payment.calc.payment_calculator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;

@RestController
@RequiredArgsConstructor
public class MyController {

    @PostMapping("/process-groups")
    public Flux<ServerSentEvent<String>> processGroups(@RequestBody GroupCalculationRequest groupCalculationRequest) {
        //System.out.println(groupCalculationRequest);
        return Flux.fromIterable(groupCalculationRequest.groups())
                .map(group -> ServerSentEvent.<String>builder()
                        .id(group.groupId())
                        .event("message")
                        .data(group.groupName())
                        .build());
    }

}
