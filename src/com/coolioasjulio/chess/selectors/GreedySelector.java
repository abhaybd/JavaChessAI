package com.coolioasjulio.chess.selectors;

import java.util.List;

public class GreedySelector implements Selector {
    @Override
    public <T> T select(List<T> toSelect, List<Double> scores) {
        int argmax = 0;
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i) > scores.get(argmax)) {
                argmax = i;
            }
        }

        return toSelect.get(argmax);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
