package org.example;

import java.util.List;

public interface TextMeasure {
    double calculate(List<String> list1, List<String> list2);
    double  calculate(String list1, String list2);
}
