package com.almasb.mathris;

import java.util.function.BiFunction;

/**
 * Defines possible operations in the game.
 * A valid operation is always between two integers and the result is also an integer.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public enum Operation {
    ADD("+", (a, b) -> a + b),

    SUB("-", (a, b) -> a - b),

    MUL("*", (a, b) -> a * b),

    DIV("/", (a, b) -> a / b);

    /**
     * How this operation is represented in the UI.
     */
    private String stringUI;

    /**
     * The actual operation implementation without error checks, e.g. div by 0.
     */
    private BiFunction<Integer, Integer, Integer> function;

    Operation(String stringUI, BiFunction<Integer, Integer, Integer> function) {
        this.stringUI = stringUI;
        this.function = function;
    }

    public String getStringUI() {
        return stringUI;
    }

    public BiFunction<Integer, Integer, Integer> getFunction() {
        return function;
    }
}
