package ru.payment.calc.payment_calculator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.payment.calc.payment_calculator.controller.dto.request.GroupCalculationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupCalculationHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Here");

        Flux<WebSocketMessage> output = session.receive()
                .map(this::toRequest)
                .flatMap(request -> Flux.fromIterable(request.groups()))
                //.delayElements(Duration.ofSeconds(5))
                .map(group -> {
                    log.info("Doing {}", group.groupName());
                    return group.groupName();
                })
                .map(value -> session.textMessage("Group: " + value));

        return session.send(output);
    }

    private GroupCalculationRequest toRequest(WebSocketMessage message) {
        try {
            return objectMapper.readValue(message.getPayloadAsText(), GroupCalculationRequest.class);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

}
