package edu.agh.bpmnai.generator.v2.functions;

import jakarta.annotation.Nullable;

import java.util.List;

import static java.util.Collections.emptyList;

public record ArgumentsParsingResult<T>(@Nullable T result, List<String> errors) {

    public ArgumentsParsingResult(@Nullable T result) {
        this(result, emptyList());
    }

    public ArgumentsParsingResult(List<String> errors) {
        this(null, List.copyOf(errors));
    }

    public boolean isError() {
        return result == null;
    }
}
