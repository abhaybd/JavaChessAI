package com.coolioasjulio.chess.selectors;

import java.util.List;
import java.util.stream.Collectors;

public class SoftmaxSelector implements Selector {
    @Override
    public <T> T select(List<T> toSelect, List<Double> scores) {
        if (toSelect.size() != scores.size() || toSelect.size() == 0 || scores.size() == 0) {
            throw new IllegalArgumentException("Must have equal sizes and nonzero!");
        }

        // Get the numerators of the softmax function, mapping 0 to 1e-8 and capping a
        // max value to prevent overflow
        List<Double> unNormalizedProbabilities = scores.stream().map(Math::exp).map(e -> e == 0.0 ? 1e-8 : e)
                .map(e -> e == Double.POSITIVE_INFINITY ? Double.MAX_VALUE / toSelect.size() : e)
                .collect(Collectors.toList());

        // Sum the numerators to get the denominator
        double denominator = unNormalizedProbabilities.stream().reduce(Double::sum)
                .orElseThrow(IllegalStateException::new);

        // Get the normalized probabilities. This array sums to 1.0
        double[] probabilities = unNormalizedProbabilities.stream().mapToDouble(d -> d / denominator).toArray();

        // Select the element
        double random = Math.random();
        for (int i = 0; i < probabilities.length; i++) {
            random -= probabilities[i];
            if (random <= 0) {
                return toSelect.get(i);
            }
        }

        // If something doesn't get selected, then the selection malfunctioned.
        throw new IllegalStateException("The softmax selection malfunctioned! The numbers do not sum to 1!");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
