//Holds declaration of base clase document with all it's params/vectors

package org.example;

public class Document{

    private String documentId;
    private FeatureVector features;

    public Document(String documentId, FeatureVector features) {
        this.documentId = documentId;
        this.features = features;
    }

    public String getDocumentId() {
        return documentId;
    }

    public FeatureVector getFeatures() {
        return features;
    }
}
