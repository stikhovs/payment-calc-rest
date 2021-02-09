package ru.payment.calc.payment_calculator.utils;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @Test
    public void test() {
        double result = Utils.parseStringToDouble("-10,00");
        assertEquals(-10.00, result);
    }

    @Test
    public void fluxTest() {

        Flux<Integer> evenNumbers = Flux.just(2, 4, 6);

        Flux<Integer> fluxOfIntegers = evenNumbers.concatWith(Flux.just(1, 3, 5));

        StepVerifier.create(fluxOfIntegers)
                .expectNext(2)
                .expectNext(4)
                .expectNext(6)
                .expectNext(1)
                .expectNext(3)
                .expectNext(5)
                .expectComplete()
                .verify();
    }

}
