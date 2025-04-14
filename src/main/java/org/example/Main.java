package org.example;

import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

//        if (args.length < 1) {
//            System.out.println("Usage: java DocumentLoader <sgm_directory>");
//            System.exit(1);
//        }

        String docDir = "C:\\Users\\avish\\Downloads\\reuters+21578+text+categorization+collection\\reuters21578.tar\\reuters21578";
        DocumentLoader loader = new DocumentLoader();
        List<Document> documents = loader.loadDocuments(docDir);

        System.out.println("Loaded " + documents.size() + " documents in total.");

        // Display some sample data
        if (!documents.isEmpty()) {
            int i = 0;
            for (Document doc : documents) {
                System.out.println("Document ID: " + doc.getDocumentId());
                System.out.println("First Name: " + doc.getFeatures().getFirstName0());
                System.out.println("Organizations: " + doc.getFeatures().getOrganisations1());
                System.out.println("Topic: " + doc.getFeatures().getPopularTopic4());
                System.out.println("Word Count: " + doc.getFeatures().getWordCount9());
                System.out.println("FeatureVector: " + doc.getFeatures().toString());
                System.out.println("=========================================");
                i++;
                if (i > 5) {
                    break;
                }
            }
        }
    }
}