package edu.agh.bpmnai.generator.v2;

import jakarta.annotation.Nullable;

import java.util.Objects;

public record Node(String id, @Nullable String label) {
    public boolean hasLabel() {
        return label != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
