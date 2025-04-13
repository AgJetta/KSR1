package org.example;

import java.util.List;

public class KNN implements Algorithm<Document> {

    private List<Document> trainingDocuments;
    private List<Document> testDocuments;

    private int k;
    private DistanceMetric distanceMetric;

    public KNN(int k, DistanceMetric distanceMetric) {
        this.k = k;
        this.distanceMetric = distanceMetric;
    }

    @Override
    public String classify(Document input, List<Document> trainingData) {
        return "";
    }


}
