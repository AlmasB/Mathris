package com.almasb.mathris;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class Player {

    // [0..10]
    private IntegerProperty streak = new SimpleIntegerProperty(0);

    public IntegerProperty streakProperty() {
        return streak;
    }

    public void addStreak() {
        if (isFullStreak())
            return;

        streak.set(streak.get() + 1);
    }

    public void clearStreak() {
        streak.set(0);
    }

    public int getStreak() {
        return streak.get();
    }

    public boolean isFullStreak() {
        return getStreak() == 10;
    }
}
