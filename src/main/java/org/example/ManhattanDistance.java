package org.example;

import java.util.List;

public class ManhattanDistance implements DistanceMetric {

    @Override
    public double aggregate(List<Double> distances) {
        double sum = 0.0;
        for (double d : distances) {
            sum += Math.abs(d);
        }
        return sum;
    }
}
