package com.almasb.mathris;

import com.almasb.fxgl.gameplay.GameDifficulty;

import java.util.Map;

import static com.almasb.fxgl.gameplay.GameDifficulty.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class Config {

    public static final int MAX_Y = 15;

    public static final Map<GameDifficulty, AIPlayerData> AI_DATA = Map.of(
            EASY, new AIPlayerData(4, 0.25),
            MEDIUM, new AIPlayerData(3, 0.5),
            HARD, new AIPlayerData(2, 0.75),
            NIGHTMARE, new AIPlayerData(1, 0.95)
    );
}
