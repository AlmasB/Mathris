package com.almasb.mathris;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.Effect;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.mathris.Config.MAX_Y;
import static com.almasb.mathris.EntityType.BLOCK;

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

    // columns
    private int minX;
    private int maxX;

    public PlayerComponent(int minX, int maxX) {
        this.minX = minX;
        this.maxX = maxX;
    }

    public void addBlock(Entity block) {
        blocks.add(block);
    }

    public List<Entity> getBlocks() {
        return blocks;
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

        if (blocks.isEmpty()) {
            app.onPlayerWon(this);
        }
    }

    public void reset() {
        blocks.clear();
        clearStreak();

        effects.endAllEffects();
    }

    public void applyNegativeEffect(NegativeEffect effect) {
        if (effect == NegativeEffect.BIG_NUMBERS) {

            // TODO: random 10, not top 10
            // TODO: AI needs to have a penalty score for big numbers, otherwise no difference
            // and for hide effect
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
        } else if (effect == NegativeEffect.EXTRA_BLOCKS) {

            IntStream.rangeClosed(minX, maxX)
                    .mapToObj(i -> getColumn(i))
                    .filter(col -> !col.isEmpty())
                    .min(Comparator.comparingInt(column -> column.size()))
                    .ifPresent(column -> {

                        var block = app.createBlock(column.get(0).getInt("x"), MAX_Y - column.size());

                        addBlock(block);
                        getGameWorld().addEntity(block);
                    });
        }
    }

    public void guess(String guess) {
        if (effects.hasEffect(FreezeEffect.class))
            return;

        blocks.stream()
                .filter(PlayerComponent::isBottomRow)
                .forEach(e -> {
                    if (e.getString("answer").equals(guess)) {
                        destroyBlock(e);
                        addStreak();

                        if (isFullStreak()) {
                            clearStreak();

                            app.getOtherPlayer(this).applyNegativeEffect(NegativeEffect.HIDE_NUMBERS);
                        }
                    }
                });
    }

    private List<Entity> getColumn(int x) {
        return blocks.stream()
                .filter(b -> b.getInt("x") == x)
                .toList();
    }

    private void destroyBlock(Entity block) {
        play("correct.wav");

        var blockX = block.getInt("x");
        var blockY = block.getInt("y");

        block.removeFromWorld();

        var blocks = byType(BLOCK)
                .stream()
                .filter(e -> e.getInt("x") == blockX && e.getInt("y") < blockY)
                .toList();

        // column has been cleared
        if (blocks.isEmpty()) {
            clearColumn(blockX);
            app.getOtherPlayer(this).applyNegativeEffect(NegativeEffect.FREEZE);

        } else {

            // drop down blocks above the one destroyed
            dropDown(blocks);
        }
    }

    private void dropDown(List<Entity> columnBlocks) {
        columnBlocks.forEach(e -> {
            var y = e.getInt("y");

            e.setProperty("y", y + 1);

            animationBuilder()
                    .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                    .duration(Duration.seconds(0.5))
                    .translate(e)
                    .from(new Point2D(e.getX(), y * 50))
                    .to(new Point2D(e.getX(), (y+1) * 50))
                    .buildAndPlay();
        });
    }

    private void clearColumn(int blockX) {
        play("column.wav");

        var highlight = new Rectangle(120, (MAX_Y+1) * 50, Color.TRANSPARENT);
        highlight.setStroke(Color.YELLOW);
        highlight.setStrokeWidth(5.5);
        highlight.setStrokeType(StrokeType.INSIDE);

        animationBuilder()
                .onFinished(() -> removeUINode(highlight))
                .duration(Duration.seconds(0.11))
                .repeat(6)
                .autoReverse(true)
                .fadeIn(highlight)
                .buildAndPlay();

        addUINode(highlight, 40 + blockX * 120, 0);
    }

    public static boolean isBottomRow(Entity block) {
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
