package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String docDir = "C:\\Users\\avish\\Downloads\\reuters+21578+text+categorization+collection\\reuters21578.tar\\reuters21578";
        DocumentLoader loader = new DocumentLoader();
        List<Document> documents = loader.loadDocuments(docDir);

        System.out.println("Loaded " + documents.size() + " documents in total.");

        // Display some sample data
        if (!documents.isEmpty()) {
//            int i = 0;
            for (Document doc : documents) {
                FeatureVector features = doc.getFeatures();
                if (features.getPopularTopic4().isEmpty()

                ) {
                    continue;
                }
                System.out.println("Document ID: " + doc.getDocumentId());
                System.out.println("Label: " + doc.getTargetLabel());
                System.out.println("First Name: " + features.getFirstName0());
                System.out.println("Organizations: " + features.getOrganisations1());
                System.out.println("Popular Country: " + features.getPopularCountry2());
                System.out.println("First Country: " + features.getFirstCountry3());
                System.out.println("Topic: " + features.getPopularTopic4());
                System.out.println("Currency: " + features.getCurrency5());
                System.out.println("Author: " + features.getAuthor6());
                System.out.println("Localisation: " + features.getLocalisation7());
                System.out.println("Day of Week: " + features.getDayOfWeek8());
                System.out.println("Word Count: " + features.getWordCount9());

                System.out.println("=========================================");
//                i++;
//                if (i > 5) {
//                    break;
//                }
            }
        }
        List<String> validPlaces = Arrays.asList("west-germany", "usa", "france", "uk", "canada", "japan");
        Hashtable<String, Integer> counts = new Hashtable<>();
        for (Document doc : documents) {
            String label = doc.getTargetLabel();
            if (validPlaces.contains(label)) {
                counts.put(label, counts.getOrDefault(label, 0) + 1);
            }
            else {
                counts.put(label + "(Invalid)", counts.getOrDefault("Invalid", 0) + 1);
            }
        }
        System.out.println("Counts of labels:");
        for (String label : counts.keySet()) {
            System.out.println(label + ": " + counts.get(label));
        }
        Integer totalLabels = 0;
        for (Integer count : counts.values()) {
            totalLabels += count;
        }
        System.out.println("Total labels: " + totalLabels);

        // Set of currencies that appear
        List<String> currencies = new ArrayList<>();
        for (Document doc : documents) {
            FeatureVector features = doc.getFeatures();
            List<String> currencyList = features.getCurrency5();
            for (String currency : currencyList) {
                if (!currencies.contains(currency)) {
                    currencies.add(currency);
                }
            }
        }
        System.out.println("Currencies: " + currencies);

    }
}