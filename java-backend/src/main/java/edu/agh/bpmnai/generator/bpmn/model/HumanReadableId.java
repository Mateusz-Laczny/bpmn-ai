package edu.agh.bpmnai.generator.bpmn.model;

import edu.agh.bpmnai.generator.v2.functions.parameter.Description;

import java.util.regex.Pattern;

public record HumanReadableId(@Description("Name of the element") String name,
                              @Description("Id of the element") String id) {

    private static final Pattern pattern = Pattern.compile(".+#.+");

    public static HumanReadableId fromString(String s) {
        String[] split = s.split("#");
        return new HumanReadableId(split[0], split[1]);
    }

    public static boolean isHumanReadableIdentifier(String s) {
        return pattern.matcher(s).find();
    }

    public String asString() {
        return name + "#" + id;
    }

    @Override
    public String toString() {
        return asString();
    }
}
