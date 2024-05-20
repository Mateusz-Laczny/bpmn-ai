package edu.agh.bpmnai.generator.datatype;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public class Result<L, R> {
    private final L value;
    private final R error;

    private Result(L value, R error) {
        this.value = value;
        this.error = error;
    }

    public static <L, R> Result<L, R> ok(L value) {
        return new Result<>(value, null);
    }

    public static <L, R> Result<L, R> error(R error) {
        return new Result<>(null, error);
    }

    public boolean isOk() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }
}
