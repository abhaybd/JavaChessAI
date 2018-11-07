package com.coolioasjulio.chess.selectors;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomSelector implements Selector {
    @Override
    public <T> T select(List<T> toSelect, List<Double> scores) {
        int i = ThreadLocalRandom.current().nextInt(toSelect.size());
        return toSelect.get(i);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
