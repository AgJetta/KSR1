//Holds declaration of base clase document with all it's params/vectors

package org.example;

public class Document{

    private String documentId;
    private FeatureVector features;
    private String targetLabel;

    public Document(String documentId, FeatureVector features, String targetLabel) {
        this.documentId = documentId;
        this.features = features;
        this.targetLabel = targetLabel;
    }

    public String getDocumentId() {
        return documentId;
    }

    public FeatureVector getFeatures() {
        return features;
    }

    public String getTargetLabel() {
        return targetLabel;
    }
}
