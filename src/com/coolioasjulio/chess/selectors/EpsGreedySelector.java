package com.coolioasjulio.chess.selectors;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EpsGreedySelector implements Selector {
    private final double epsilon;
    private final Selector greedy, random;

    /**
     * Selector that chooses either the best option or a random option. Best option
     * has probability epsilon. Random option has probability 1-epsilon.
     *
     * @param epsilon Probability of choosing best option.
     */
    public EpsGreedySelector(double epsilon) {
        this.epsilon = epsilon;
        this.greedy = new GreedySelector();
        this.random = new RandomSelector();
    }

    @Override
    public <T> T select(List<T> toSelect, List<Double> scores) {
        if (ThreadLocalRandom.current().nextDouble() <= epsilon) {
            return greedy.select(toSelect, scores);
        } else {
            return random.select(toSelect, scores);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
