package com.almasb.mathris;

import com.almasb.fxgl.entity.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class Player {

    // [0..10]
    private IntegerProperty streak = new SimpleIntegerProperty(0);

    private List<Entity> blocks = new ArrayList<>();

    public void addBlock(Entity block) {
        blocks.add(block);
        block.setOnNotActive(() -> blocks.remove(block));
    }

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

    public void applyNegativeEffect(NegativeEffect effect) {
        if (effect == NegativeEffect.BIG_NUMBERS) {

            // TODO: random 10, not top 10
            // TODO: AI needs to have a penalty score for big numbers, otherwise no difference
            blocks.stream()
                    .limit(10)
                    .forEach(block -> {
                        animationBuilder()
                                .duration(Duration.seconds(0.35))
                                .repeat(2)
                                .autoReverse(true)
                                .fadeOut(block)
                                .buildAndPlay();

                        runOnce(() -> {
                            var a = random(100, 500);
                            var b = random(100, 500);

                            block.setProperty("question", "" + a + "+" + b);
                            block.setProperty("answer", "" + (a+b));
                        }, Duration.seconds(0.35));
                    });
        }
    }
}
