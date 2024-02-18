package edu.agh.bpmnai.generator;

import java.util.List;

public class Text {
    public static String createTextList(List<String> listElements, String listElementIndicator) {
        var stringBuilder = new StringBuilder();
        for (String listElement : listElements) {
            stringBuilder.append(listElementIndicator).append(" ").append(listElement).append('\n');
        }

        return stringBuilder.toString();
    }
}
