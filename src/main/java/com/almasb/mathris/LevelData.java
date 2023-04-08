package com.almasb.mathris;

import java.util.List;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public record LevelData(
        int minValue,
        int maxValue,
        List<Operation> availableOperations
) { }
