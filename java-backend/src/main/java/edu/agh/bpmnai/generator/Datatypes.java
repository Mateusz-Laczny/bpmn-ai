package edu.agh.bpmnai.generator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

public class Datatypes {
    @EqualsAndHashCode
    @ToString
    @Getter
    public static class Either<L, R> {
        private final L left;
        private final R right;

        private Either(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public static <L, R> Either<L, R> asLeft(L leftValue) {
            return new Either<>(leftValue, null);
        }

        public static <L, R> Either<L, R> asRight(R rightValue) {
            return new Either<>(null, rightValue);
        }

        public boolean isLeft() {
            return left != null;
        }

        public boolean isRight() {
            return right != null;
        }
    }
}
