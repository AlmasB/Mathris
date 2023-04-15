package com.almasb.mathris;

import com.almasb.fxgl.gameplay.GameDifficulty;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.gameplay.GameDifficulty.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class Config {

    public static final int MAX_Y = 15;

    public static final Duration FREEZE_DURATION = Duration.seconds(3);
    public static final Duration HIDE_DURATION = Duration.seconds(4);

    public static final Duration BONUS_SCORE_INTERVAL = Duration.seconds(0.5);

    public static final int BASE_SCORE = 100;
    public static final int SCORE_MULTIPLIER = 2;

    // for now max answer is 4 digits
    public static final Map<GameDifficulty, AIPlayerData> AI_DATA = Map.of(
            EASY,      new AIPlayerData(3, 0.60, 0.15),
            MEDIUM,    new AIPlayerData(2, 0.70, 0.10),
            HARD,      new AIPlayerData(2, 0.85, 0.05),
            NIGHTMARE, new AIPlayerData(1, 1.00, 0.05)
    );
}
