package org.example;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        // DEV: .SGM FILES WITH THE DATA, REPLACE FOR YOUR PATH
        String docDir;
        if (args.length > 0) {
            docDir = args[0];
        } else {
            throw new IllegalArgumentException("Please provide the path to the directory containing .sgm files.");
        }

        List<org.example.Document> documents = new ArrayList<>();
        DocumentLoader loader = new DocumentLoader();
        try {
            documents = loader.loadDocuments(docDir);
        }
        catch (Exception e) {
            System.err.println("Error loading documents: " + e.getMessage());
            System.err.println("CHECK IF THE DATA DIRECTORY IS CORRECT");
        }

        System.out.println("Loaded " + documents.size() + " documents in total.");
        if (documents.isEmpty()) {
            System.err.println("No documents found. CHECK DIRECTORY PATH OR FILES.  DocumentLoader.java -> main method");
            return;
        }

        // Initialize metrics and similarity measures
        DistanceMetric euclidean = new EuclideanDistance();
        DistanceMetric manhattan = new ManhattanDistance();
        DistanceMetric chebyshev = new ChebyshevDistance();
        TextMeasure cosine = new CosineTextMeasure();
        TextMeasure jaccard = new JaccardTextMeasure();

        // 1. Compare classification results for different k values
        System.out.println("\n==== Experiment 1: Impact of k value ====");
        Set<Integer> someFeatures = IntStream.range(0, 3).boxed().collect(Collectors.toSet());
        for (int k : new int[]{1, 2, 3, 4}) {
            KNN classifier = new KNN(k, 0.6, someFeatures, euclidean, cosine);
            classifier.splitDataset(documents);
            classifier.normalizeNumericalFeatures();
            classifier.evaluateModel();

            System.out.printf("k = %2d: Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    k, classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // 2. Compare classification results for different train/test ratios
        System.out.println("\n==== Experiment 2: Impact of train/test ratio ====");
        int bestK = 5;  // Use the best k from the previous experiment
        for (double ratio : new double[]{0.3, 0.5, 0.7, 0.8, 0.9}) {
            KNN classifier = new KNN(bestK, ratio, someFeatures, euclidean, cosine);
            classifier.splitDataset(documents);
            classifier.normalizeNumericalFeatures();
            classifier.evaluateModel();

            System.out.printf("Train/Test ratio = %.1f/%.1f: Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    ratio, 1-ratio, classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // 3. Compare classification results for different metrics
        System.out.println("\n==== Experiment 3: Impact of distance metric ====");
        Map<String, DistanceMetric> metrics = new LinkedHashMap<>();
        metrics.put("Euclidean", euclidean);
        metrics.put("Manhattan", manhattan);
        metrics.put("Chebyshev", chebyshev);

        for (Map.Entry<String, DistanceMetric> entry : metrics.entrySet()) {
            KNN classifier = new KNN(bestK, 0.7, someFeatures, entry.getValue(), cosine);
            classifier.splitDataset(documents);
            classifier.normalizeNumericalFeatures();
            classifier.evaluateModel();

            System.out.printf("Metric = %s: Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    entry.getKey(), classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // Experiment with text similarity measures
        System.out.println("\n==== Experiment 3b: Impact of text similarity measures ====");
        Map<String, TextMeasure> similarities = new LinkedHashMap<>();
        similarities.put("Cosine", cosine);
        similarities.put("Jaccard", jaccard);

        for (Map.Entry<String, TextMeasure> entry : similarities.entrySet()) {
            KNN classifier = new KNN(bestK, 0.7, someFeatures, euclidean, entry.getValue());
            classifier.splitDataset(documents);
            classifier.normalizeNumericalFeatures();
            classifier.evaluateModel();

            System.out.printf("Text Similarity = %s: Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    entry.getKey(), classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // 4. Compare classification results for different feature subsets
        System.out.println("\n==== Experiment 4: Impact of feature selection ====");

        // Define 4 different feature subsets
        List<Set<Integer>> featureSubsets = new ArrayList<>();
        featureSubsets.add(Set.of(0, 1, 2, 3, 4));            // First 5 features
        featureSubsets.add(Set.of(5, 6, 7, 8, 9));            // Last 5 features
        featureSubsets.add(Set.of(0, 2, 4, 6, 8));            // Even indices
        featureSubsets.add(Set.of(1, 3, 5, 7, 9));            // Odd indices

        String[] featureNames = {
                "firstName", "organisations", "popularCountry", "firstCity", "popularTopic",
                "currency", "author", "localisation", "dayOfWeek", "wordCount"
        };

        for (int i = 0; i < featureSubsets.size(); i++) {
            Set<Integer> subset = featureSubsets.get(i);
            KNN classifier = new KNN(bestK, 0.7, subset, euclidean, cosine);
            classifier.splitDataset(documents);
            classifier.normalizeNumericalFeatures();
            classifier.evaluateModel();

            System.out.printf("Feature subset %d (%s): Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    i+1,
                    subset.stream().map(idx -> featureNames[idx]).collect(Collectors.joining(", ")),
                    classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // 5. Show per-class metrics for the best configuration
        System.out.println("\n==== Detailed metrics for best configuration ====");
        KNN bestClassifier = new KNN(bestK, 0.7, someFeatures, euclidean, cosine);
        bestClassifier.splitDataset(documents);
        bestClassifier.normalizeNumericalFeatures();
        bestClassifier.evaluateModel();

        System.out.println("Overall metrics:");
        System.out.printf("Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                bestClassifier.getAccuracy(), bestClassifier.getPrecision(), bestClassifier.getRecall(), bestClassifier.getF1());

        System.out.println("\nPer-class metrics:");
        Set<String> categories = bestClassifier.getTestDocuments().stream()
                .map(Document::getTargetLabel)
                .collect(Collectors.toSet());

        for (String category : categories) {
            System.out.printf("Class '%s': Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    category, bestClassifier.getPrecision(category), bestClassifier.getRecall(category), bestClassifier.getF1(category));
        }
    }
}