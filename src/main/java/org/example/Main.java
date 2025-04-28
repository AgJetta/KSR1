package org.example;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    // Sample dataset for demonstration
    private static List<Document> createSampleDataset(int size) {
        List<Document> documents = new ArrayList<>();
        Random random = new Random(42);  // Fixed seed for deterministic results

        String[] categories = {"politics", "sports", "technology", "entertainment"};
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emma"};
        String[][] organisations = {
                {"Apple", "Google", "Microsoft"},
                {"CNN", "BBC", "FOX"},
                {"NBA", "NFL", "FIFA"}
        };
        String[] countries = {"USA", "UK", "Germany", "France", "China", "Japan"};
        String[] cities = {"New York", "London", "Berlin", "Paris", "Beijing", "Tokyo"};
        String[] topics = {"AI", "Climate", "Economy", "Healthcare", "Education"};
        String[][] currencies = {
                {"USD", "EUR", "GBP"},
                {"JPY", "CNY", "RUB"},
                {"BTC", "ETH", "XRP"}
        };
        String[] authors = {"Smith", "Johnson", "Brown", "Wilson", "Taylor"};
        String[] localisations = {"North", "South", "East", "West", "Central"};

        for (int i = 0; i < size; i++) {
            String id = "doc_" + i;
            String category = categories[random.nextInt(categories.length)];

            // Create feature vector
            String firstName = firstNames[random.nextInt(firstNames.length)];
            List<String> orgs = Arrays.asList(organisations[random.nextInt(organisations.length)]);
            String country = countries[random.nextInt(countries.length)];
            String city = cities[random.nextInt(cities.length)];
            String topic = topics[random.nextInt(topics.length)];
            List<String> currency = Arrays.asList(currencies[random.nextInt(currencies.length)]);
            String author = authors[random.nextInt(authors.length)];
            String localisation = localisations[random.nextInt(localisations.length)];
            int dayOfWeek = random.nextInt(7) + 1;
            int wordCount = random.nextInt(1000) + 100;

            FeatureVector featureVector = new FeatureVector(
                    firstName, orgs, country, city, topic, currency, author, localisation, dayOfWeek, wordCount
            );

            documents.add(new Document(id, featureVector, category));
        }

        return documents;
    }

    public static void main(String[] args) {
        // Create a sample dataset
        List<Document> documents = createSampleDataset(1000);

        // Initialize metrics and similarity measures
        DistanceMetric euclidean = new EuclideanDistance();
        DistanceMetric manhattan = new ManhattanDistance();
        DistanceMetric chebyshev = new ChebyshevDistance();
        TextSimilarityMeasure cosine = new CosineSimilarity();
        TextSimilarityMeasure jaccard = new JaccardSimilarity();

        // 1. Compare classification results for different k values
        System.out.println("\n==== Experiment 1: Impact of k value ====");
        Set<Integer> allFeatures = IntStream.range(0, 10).boxed().collect(Collectors.toSet());
        for (int k : new int[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19}) {
            KNN classifier = new KNN(k, 0.7, allFeatures, euclidean, cosine);
            classifier.splitDataset(documents);
            classifier.evaluateModel();

            System.out.printf("k = %2d: Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    k, classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // 2. Compare classification results for different train/test ratios
        System.out.println("\n==== Experiment 2: Impact of train/test ratio ====");
        int bestK = 5;  // Use the best k from the previous experiment
        for (double ratio : new double[]{0.5, 0.6, 0.7, 0.8, 0.9}) {
            KNN classifier = new KNN(bestK, ratio, allFeatures, euclidean, cosine);
            classifier.splitDataset(documents);
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
            KNN classifier = new KNN(bestK, 0.7, allFeatures, entry.getValue(), cosine);
            classifier.splitDataset(documents);
            classifier.evaluateModel();

            System.out.printf("Metric = %s: Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    entry.getKey(), classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // Experiment with text similarity measures
        System.out.println("\n==== Experiment 3b: Impact of text similarity measures ====");
        Map<String, TextSimilarityMeasure> similarities = new LinkedHashMap<>();
        similarities.put("Cosine", cosine);
        similarities.put("Jaccard", jaccard);

        for (Map.Entry<String, TextSimilarityMeasure> entry : similarities.entrySet()) {
            KNN classifier = new KNN(bestK, 0.7, allFeatures, euclidean, entry.getValue());
            classifier.splitDataset(documents);
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
            classifier.evaluateModel();

            System.out.printf("Feature subset %d (%s): Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    i+1,
                    subset.stream().map(idx -> featureNames[idx]).collect(Collectors.joining(", ")),
                    classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
        }

        // 5. Show per-class metrics for the best configuration
        System.out.println("\n==== Detailed metrics for best configuration ====");
        KNN bestClassifier = new KNN(bestK, 0.7, allFeatures, euclidean, cosine);
        bestClassifier.splitDataset(documents);
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