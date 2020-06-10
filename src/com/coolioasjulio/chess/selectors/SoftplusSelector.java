package com.coolioasjulio.chess.selectors;

import java.util.Arrays;
import java.util.List;

public class SoftplusSelector implements Selector{
    @Override
    public <T> T select(List<T> toSelect, List<Double> scores) {
        double[] softplus = scores.stream().mapToDouble(this::softplus).toArray();
        double sum = Arrays.stream(softplus).sum();
        double rand = Math.random() * sum;

        for (int i = 0; i < softplus.length; i++) {
            rand -= softplus[i];
            if (rand <= 0) {
                return toSelect.get(i);
            }
        }
        return toSelect.get(toSelect.size()-1);
    }

    private double softplus(double d) {
        return d >= 15 ? d : Math.log1p(Math.exp(d));
    }

    @Override
    public String toString() {
        return "SoftplusSelector";
    }
}
