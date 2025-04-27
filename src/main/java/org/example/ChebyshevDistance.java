package org.example;

import java.util.List;

public class ChebyshevDistance implements DistanceMetric {

    @Override
    public double aggregate(List<Double> distances) {
        double max = 0.0;
        for (double d : distances) {
            double abs = Math.abs(d);
            if (abs > max) {
                max = abs;
            }
        }
        return max;
    }
}
