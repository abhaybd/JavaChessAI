package com.coolioasjulio.chess.selectors;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Selector {
    @Override
    String toString();

    <T> T select(List<T> toSelect, List<Double> scores);

    default <T> T select(List<T> toSelect, Function<T, Double> scoreFunction) {
        List<Double> scores = toSelect.stream().map(scoreFunction).collect(Collectors.toList());
        return select(toSelect, scores);
    }
}
