package com.almasb.mathris;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.ui.FontType;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.mathris.Config.MAX_Y;
import static com.almasb.mathris.EntityType.*;
import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class MathrisApp extends GameApplication {

    private Text output;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1600);
        settings.setHeight(900);
    }

    @Override
    protected void initInput() {
        onKeyDown(KeyCode.BACK_SPACE, () -> {
            if (output.getText().isEmpty())
                return;

            output.setText(output.getText().substring(0, output.getText().length() - 1));
        });

        onKeyDown(KeyCode.ENTER, () -> {
            var answer = output.getText();

            byType(BLOCK)
                    .stream()
                    // bottom row
                    .filter(e -> e.getInt("y") == MAX_Y)
                    .forEach(e -> {
                        if (e.getString("answer").equals(answer)) {
                            destroyBlock(e);
                        }
                    });


            output.setText("");
        });

        // TODO: duplicate
        onKeyDown(KeyCode.SPACE, () -> {
            var answer = output.getText();

            byType(BLOCK)
                    .stream()
                    // bottom row
                    .filter(e -> e.getInt("y") == MAX_Y)
                    .forEach(e -> {
                        if (e.getString("answer").equals(answer)) {
                            destroyBlock(e);
                        }
                    });


            output.setText("");
        });

        getInput().addEventHandler(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().isEmpty())
                return;

            var ch = event.getCharacter().charAt(0);

            if (Character.isDigit(ch))
                output.setText(output.getText() + event.getCharacter());
        });
    }

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new MathrisFactory());

        for (int y = 0; y <= MAX_Y; y++) {
            for (int x = 0; x < 6; x++) {
                var a = random(1, 50);
                var b = random(1, 50);

                spawn("block",
                        new SpawnData(80 + x * 120, y * 50)
                                .put("color", Color.DARKGRAY)
                                .put("x", x)
                                .put("y", y)
                                .put("question", "" + a + "+" + b)
                                .put("answer", "" + (a+b))
                        //.put("color", FXGLMath.randomColorHSB(0.4, 0.75))
                );
            }
        }

//        for (int y = 0; y < 15; y++) {
//            for (int x = 7; x < 13; x++) {
//                spawn("block",
//                        new SpawnData(20 + x * 120, 40 + y * 50)
//                                .put("color", Color.DARKSEAGREEN)
//                                .put("y", y)
//                        //.put("color", FXGLMath.randomColorHSB(0.4, 0.75))
//                );
//            }
//        }
    }

    @Override
    protected void initUI() {
        output = getUIFactoryService().newText("", Color.WHITE, FontType.MONO, 22.0);

        addUINode(output, 150, getAppHeight() - 70);

        var line = new Line(0, 0, 0, 20);
        line.setStroke(Color.WHITE);
        line.setStrokeWidth(5);

        addUINode(line, 130, getAppHeight() - 80);

        // UI

        var ui = new PlayerUI();

        addUINode(ui);


        var aiText = getUIFactoryService().newText("AI /\n2nd player goes here", Color.WHITE, FontType.MONO, 45.0);

        addUINode(aiText, 900, 400);
    }

    private void destroyBlock(Entity block) {
        var blockX = block.getInt("x");
        var blockY = block.getInt("y");

        block.removeFromWorld();

        var blocks = byType(BLOCK)
                .stream()
                .filter(e -> e.getInt("x") == blockX && e.getInt("y") < blockY)
                .toList();

        if (blocks.isEmpty()) {
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

            addUINode(highlight, 80 + blockX * 120, 0);

//            byType(BLOCK)
//                    .stream()
//                    .filter(e -> e.getInt("y") == MAX_Y)
//                    .forEach(this::destroyBlock);
        } else {
            blocks.forEach(e -> {
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
