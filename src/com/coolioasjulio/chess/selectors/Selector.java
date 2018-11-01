package com.coolioasjulio.chess.selectors;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.coolioasjulio.chess.MoveCandidate;

public interface Selector {
    public String toString();

    <T> T select(List<T> toSelect, List<Double> scores);

    default <T> T select(List<T> toSelect, Function<T, Double> scoreFunction) {
        List<Double> scores = toSelect.stream().map(scoreFunction::apply).collect(Collectors.toList());
        return select(toSelect, scores);
    }

    default MoveCandidate select(List<MoveCandidate> candidates) {
        return select(candidates, MoveCandidate::getScore);
    }
}
