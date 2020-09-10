package ru.payment.calc.payment_calculator.utils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {

    public static <T> void nonNullSet(T setCandidate, Consumer<T> ifNonNullCandidateConsumer) {
        Optional.ofNullable(setCandidate).ifPresent(ifNonNullCandidateConsumer);
    }

    public static <T> T nonNullGet(Object getCandidate, Supplier<T> ifNonNullCandidateSupplier) {
        return Optional.ofNullable(getCandidate).isPresent() ? ifNonNullCandidateSupplier.get() : null;
    }

}
