package org.example;

import java.util.List;

public interface Algorithm<T>{

    String classify(T input, List<T> trainingData);


}
