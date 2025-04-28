package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class KNN {

    private int k;
    private double trainRatio;
    private Set<Integer> selectedFeatureIndices;
    private DistanceMetric distanceMetric;
    private TextSimilarityMeasure textSimilarityMeasure;

    private List<Document> allDocuments;
    private List<Document> trainingDocuments;
    private List<Document> testDocuments;

    private Map<String, Integer> truePositives;
    private Map<String, Integer> falsePositives;
    private Map<String, Integer> falseNegatives;
    private int correctPredictions;

    public KNN(int k, double trainRatio, Set<Integer> selectedFeatureIndices,
               DistanceMetric distanceMetric, TextSimilarityMeasure textSimilarityMeasure) {
        this.k = k;
        this.trainRatio = trainRatio;
        this.selectedFeatureIndices = selectedFeatureIndices;
        this.distanceMetric = distanceMetric;
        this.textSimilarityMeasure = textSimilarityMeasure;

        this.truePositives = new HashMap<>();
        this.falsePositives = new HashMap<>();
        this.falseNegatives = new HashMap<>();
    }

    // Deterministic
    public void splitDataset(List<Document> documents) {
        this.allDocuments = new ArrayList<>(documents);
        int trainingSize = (int) (documents.size() * trainRatio);

        this.trainingDocuments = documents.subList(0, trainingSize);
        this.testDocuments = documents.subList(trainingSize, documents.size());
    }

    // Single document
    public String classify(Document document) {
        List<DocumentDistance> distances = new ArrayList<>();

        for (Document trainDoc : trainingDocuments) {
            double distance = calculateDistance(document, trainDoc);
            distances.add(new DocumentDistance(trainDoc, distance));
        }

        // Ascending sort
        distances.sort(Comparator.comparingDouble(DocumentDistance::distance));

        List<DocumentDistance> nearestNeighbors = distances.subList(0, Math.min(k, distances.size()));

        Map<String, Integer> classCounts = new HashMap<>();
        for (DocumentDistance neighbor : nearestNeighbors) {
            String docClass = neighbor.document().getTargetLabel();
            classCounts.put(docClass, classCounts.getOrDefault(docClass, 0) + 1);
        }

        return classCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private double calculateDistance(Document doc1, Document doc2) {
        double distance = 0.0;

        // Handle numeric features with the chosen distance metric
        if (selectedFeatureIndices.contains(8) || selectedFeatureIndices.contains(9)) {
            distance += distanceMetric.calculate(doc1, doc2, selectedFeatureIndices);
        }

        if (selectedFeatureIndices.stream().anyMatch(i -> i < 8)) {
            distance += calculateTextDistance(doc1, doc2);
        }

        return distance;
    }

    // For categorical features
    private double calculateTextDistance(Document doc1, Document doc2) {
        double textDistance = 0.0;
        FeatureVector vec1 = doc1.getFeatures();
        FeatureVector vec2 = doc2.getFeatures();

        // Single categorical features
        if (selectedFeatureIndices.contains(0)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(vec1.getFirstName0(), vec2.getFirstName0());
        }
        if (selectedFeatureIndices.contains(2)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(vec1.getPopularCountry2(), vec2.getPopularCountry2());
        }
        if (selectedFeatureIndices.contains(3)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(vec1.getFirstCity3(), vec2.getFirstCity3());
        }
        if (selectedFeatureIndices.contains(4)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(vec1.getPopularTopic4(), vec2.getPopularTopic4());
        }
        if (selectedFeatureIndices.contains(6)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(vec1.getAuthor6(), vec2.getAuthor6());
        }
        if (selectedFeatureIndices.contains(7)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(vec1.getLocalisation7(), vec2.getLocalisation7());
        }

        // List-based categorical features
        if (selectedFeatureIndices.contains(1)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(
                    String.join(",", vec1.getOrganisations1()),
                    String.join(",", vec2.getOrganisations1()));
        }
        if (selectedFeatureIndices.contains(5)) {
            textDistance += textSimilarityMeasure.calculateSimilarity(
                    String.join(",", vec1.getCurrency5()),
                    String.join(",", vec2.getCurrency5()));
        }

        return textDistance;
    }

    // Run classification on the test set and calculate metrics
    public void evaluateModel() {
        truePositives.clear();
        falsePositives.clear();
        falseNegatives.clear();
        correctPredictions = 0;

        // Get all unique categories
        Set<String> categories = allDocuments.stream()
                .map(Document::getTargetLabel)
                .collect(Collectors.toSet());

        // Initialize counters for each category
        for (String category : categories) {
            truePositives.put(category, 0);
            falsePositives.put(category, 0);
            falseNegatives.put(category, 0);
        }

        // Classify each test document and update metrics
        for (Document testDoc : testDocuments) {
            String actualClass = testDoc.getTargetLabel();
            String predictedClass = classify(testDoc);

            if (predictedClass.equals(actualClass)) {
                correctPredictions++;
                truePositives.put(actualClass, truePositives.get(actualClass) + 1);
            } else {
                falsePositives.put(predictedClass, falsePositives.get(predictedClass) + 1);
                falseNegatives.put(actualClass, falseNegatives.get(actualClass) + 1);
            }
        }
    }

    // Calculate accuracy
    public double getAccuracy() {
        return (double) correctPredictions / testDocuments.size();
    }

    // Calculate precision for all documents
    public double getPrecision() {
        int totalTP = truePositives.values().stream().mapToInt(Integer::intValue).sum();
        int totalFP = falsePositives.values().stream().mapToInt(Integer::intValue).sum();

        return totalTP == 0 ? 0 : (double) totalTP / (totalTP + totalFP);
    }

    // Calculate precision for a specific class
    public double getPrecision(String category) {
        int tp = truePositives.getOrDefault(category, 0);
        int fp = falsePositives.getOrDefault(category, 0);

        return tp == 0 ? 0 : (double) tp / (tp + fp);
    }

    // Calculate recall for all documents
    public double getRecall() {
        int totalTP = truePositives.values().stream().mapToInt(Integer::intValue).sum();
        int totalFN = falseNegatives.values().stream().mapToInt(Integer::intValue).sum();

        return totalTP == 0 ? 0 : (double) totalTP / (totalTP + totalFN);
    }

    // Calculate recall for a specific class
    public double getRecall(String category) {
        int tp = truePositives.getOrDefault(category, 0);
        int fn = falseNegatives.getOrDefault(category, 0);

        return tp == 0 ? 0 : (double) tp / (tp + fn);
    }

    // Calculate F1 score
    public double getF1() {
        double precision = getPrecision();
        double recall = getRecall();

        return (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
    }

    // Calculate F1 score for a specific class
    public double getF1(String category) {
        double precision = getPrecision(category);
        double recall = getRecall(category);

        return (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
    }

    public int getK() {
        return k;
    }

    public double getTrainRatio() {
        return trainRatio;
    }

    public Set<Integer> getSelectedFeatureIndices() {
        return selectedFeatureIndices;
    }

    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    public List<Document> getTrainingDocuments() {
        return trainingDocuments;
    }

    public List<Document> getTestDocuments() {
        return testDocuments;
    }

    private record DocumentDistance(Document document, double distance) {}
}