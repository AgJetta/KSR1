package org.example;

import java.util.List;

public class EuclideanDistance implements DistanceMetric {

    @Override
    public double aggregate(List<Double> distances) {
        double sum = 0.0;
        for (double d : distances) {
            sum += d * d;
        }
        return Math.sqrt(sum);
    }
}
