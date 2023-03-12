package ru.payment.calc.payment_calculator.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.payment.calc.payment_calculator.model.MyStudent;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebClientController {

    @GetMapping("/react-test")
    public MyStudent reactTest() {
        MyStudent student = new MyStudent("sergio", LocalTime.now().toString());
        log.info("{}", student);
        return student;
    }

    @GetMapping(value = "/my-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> testSse() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(sequence))
                        .event("message")
                        .data("SSE - " + LocalTime.now().toString())
                        .build())
                .doOnEach(sseSignal -> log.info(sseSignal.toString()));
    }

    @GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux - " + LocalTime.now().toString());
    }

    @GetMapping(path = "/stream-flux-1")
    public Flux<ServerSentEvent<String>> streamFlux1() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String> builder()
                        .id(String.valueOf(sequence))
                        .event("periodic-event")
                        .data("SSE - " + LocalTime.now().toString())
                        .build());
    }

    @SneakyThrows
    @GetMapping("/test")
    public CompletableFuture<String> test() {
        CompletableFuture<Void> task = CompletableFuture
                .runAsync(() -> {
                    for (int i = 0; i < 5; i++) {
                        System.out.println("Do task: " + i);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
        return task
                .thenApplyAsync(aVoid -> "Process started");

        /*UUID uuid = UUID.randomUUID();
        boolean isAdded = blockingDeque.add(uuid);
        if (isAdded) {
            return uuid.toString();
        }
        return "No!";*/
    }

}
