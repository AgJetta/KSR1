package org.example;

import java.util.*;

import static org.example.UI.createAndPrintConfusionMatrix;
import static org.example.UI.runClassifier;

public class Main {
    public static void main(String[] args) {
        String docDir;
        if (args.length > 0) {
            docDir = args[0];
        } else {
            throw new IllegalArgumentException("Please provide the path to the directory containing .sgm files.");
        }

        DocumentLoader loader = new DocumentLoader();
        List<org.example.Document> documents;
        try {
            documents = loader.loadDocuments(docDir);
        }
        catch (Exception e) {
            System.err.println("Error loading documents: " + e.getMessage());
            System.err.println("CHECK IF THE DATA DIRECTORY IS CORRECT");
            return;
        }

        boolean isPredefined = false;
        if (args.length >1) {
            isPredefined = Boolean.parseBoolean(args[1]);
        }

        if (isPredefined) {
            predefined_experiments(documents);
            return;
        }

        UI ui = new UI(documents);
        ui.start();
    }

    public static void predefined_experiments(List<org.example.Document> documents) {
        if (documents.isEmpty()) {
            System.err.println("No documents found. CHECK DIRECTORY PATH OR FILES.  DocumentLoader.java -> main method");
            return;
        }

        int bestK = 3;
        double bestRatio = 0.7;

        DistanceMetric euclidean = new EuclideanDistance();
        DistanceMetric manhattan = new ManhattanDistance();
        DistanceMetric chebyshev = new ChebyshevDistance();
        TextMeasure jaccard = new JaccardTextMeasure();
        TextMeasure levenshtein = new LevenshteinTextMeasure();

        Set<Integer> allFeatures = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        System.out.println("\n==== Experiment 1: Impact of k value ====");
        for (int k : new int[]{1, 3, 5, 10, 25, 75, 100, 150, 250, 400}) {
            runClassifier(k, bestRatio, allFeatures, euclidean, levenshtein, documents);
        }

        System.out.println("\n==== Experiment 2: Impact of train/test ratio ====");
        for (double ratio : new double[]{0.1, 0.3, 0.5, 0.7, 0.9}) {
            runClassifier(bestK, ratio, allFeatures, euclidean, levenshtein, documents);
        }

        System.out.println("\n==== Experiment 3: Impact of distance metric ====");
        Map<String, DistanceMetric> metrics = new LinkedHashMap<>();
        metrics.put("Euclidean", euclidean);
        metrics.put("Manhattan", manhattan);
        metrics.put("Chebyshev", chebyshev);

        for (Map.Entry<String, DistanceMetric> entry : metrics.entrySet()) {
            runClassifier(bestK, bestRatio, allFeatures, entry.getValue(), levenshtein, documents);
        }

        System.out.println("\n==== Experiment 3b: Impact of text similarity measures ====");
        Map<String, TextMeasure> similarities = new LinkedHashMap<>();
        similarities.put("Levenshtein", levenshtein);
        similarities.put("Jaccard", jaccard);

        for (Map.Entry<String, TextMeasure> entry : similarities.entrySet()) {
            runClassifier(bestK, bestRatio, allFeatures, euclidean, entry.getValue(), documents);
        }

        System.out.println("\n==== Experiment 4: Impact of feature selection ====");

        List<Set<Integer>> featureSubsets = new ArrayList<>();
        featureSubsets.add(Set.of(0, 1));
        featureSubsets.add(Set.of(0, 1, 2, 3, 4));
        featureSubsets.add(Set.of(5, 6));
        featureSubsets.add(Set.of(5, 6, 7, 8, 9));

        for (Set<Integer> subset : featureSubsets) {
            runClassifier(bestK, bestRatio, subset, manhattan, levenshtein, documents);
        }

        System.out.println("\n==== Experiment 5: Perfect configuration ====");
        KNN classifier = runClassifier(bestK, bestRatio, Set.of(5, 6, 7, 8, 9), manhattan, levenshtein, documents);

        List<String> actualLabels = classifier.getTestDocuments()
                .stream()
                .map(org.example.Document::getTargetLabel)
                .toList();
        List<String> predictedLabels = classifier.getTestDocuments()
                .stream()
                .map(org.example.Document::getPredictedLabel)
                .toList();

        createAndPrintConfusionMatrix(actualLabels, predictedLabels);
    }

}