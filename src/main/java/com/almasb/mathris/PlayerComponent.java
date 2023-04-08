package com.almasb.mathris;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.Effect;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.mathris.Config.MAX_Y;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
@Required(EffectComponent.class)
public final class PlayerComponent extends Component {

    private static final MathrisApp app = FXGL.getAppCast();

    private EffectComponent effects;

    // [0..10]
    private IntegerProperty streak = new SimpleIntegerProperty(0);

    private List<Entity> blocks = new ArrayList<>();

    private boolean isFrozen = false;

    public void addBlock(Entity block) {
        blocks.add(block);
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

    @Override
    public void onUpdate(double tpf) {
        blocks.removeIf(b -> !b.isActive());
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
        } else if (effect == NegativeEffect.FREEZE) {
            effects.startEffect(new FreezeEffect());
        } else if (effect == NegativeEffect.HIDE_NUMBERS) {
            effects.startEffect(new HideEffect());
        }
    }

    public void guess(String guess) {
        if (effects.hasEffect(FreezeEffect.class))
            return;

        blocks.stream()
                .filter(PlayerComponent::isBottomRow)
                .forEach(e -> {
                    if (e.getString("answer").equals(guess)) {
                        app.destroyBlock(e);
                        addStreak();

                        if (isFullStreak()) {
                            clearStreak();

                            app.getOtherPlayer(this)
                                    .applyNegativeEffect(NegativeEffect.HIDE_NUMBERS);
                        }
                    }
                });
    }

    private static boolean isBottomRow(Entity block) {
        return block.getInt("y") == MAX_Y;
    }
    
    private class FreezeEffect extends Effect {

        public FreezeEffect() {
            super(Config.FREEZE_DURATION);
        }

        @Override
        public void onStart(Entity entity) {
            blocks.forEach(b -> {
                b.setOpacity(0.25);
            });
        }

        @Override
        public void onEnd(Entity entity) {
            blocks.forEach(b -> {
                b.setOpacity(1.0);
            });
        }
    }
    
    public class HideEffect extends Effect {

        public HideEffect() {
            super(Config.HIDE_DURATION);
        }

        @Override
        public void onStart(Entity entity) {
            blocks.forEach(b -> {
                Text text = b.getObject("text");
                var original = text.getText();
                var newText = original.substring(0, original.length() - 2) + "??";
                text.setText(newText);
            });
        }

        @Override
        public void onEnd(Entity entity) {
            blocks.forEach(b -> {
                Text text = b.getObject("text");

                text.setText(b.getString("question"));
            });
        }
    }
}
