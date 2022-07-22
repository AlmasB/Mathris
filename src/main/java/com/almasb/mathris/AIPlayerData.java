package com.almasb.mathris;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public record AIPlayerData(
        // in seconds
        double guessInterval,

        // in [0..1], 0 = 0%, 1 = 100% correct
        double accuracy
) { }
