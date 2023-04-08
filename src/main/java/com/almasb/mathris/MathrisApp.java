package com.almasb.mathris;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.gameplay.GameDifficulty;
import com.almasb.fxgl.ui.FontType;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.mathris.Config.AI_DATA;
import static com.almasb.mathris.Config.MAX_Y;
import static com.almasb.mathris.EntityType.*;
import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class MathrisApp extends GameApplication {

    private Text output1;
    private Text output2;

    private boolean isAIReadyToGuess;

    private PlayerComponent player1;
    private PlayerComponent player2;

    private List<LevelData> levels;
    private LevelData currentLevel;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1600);
        settings.setHeight(900);
        settings.setMainMenuEnabled(false);
        settings.setGameMenuEnabled(false);
    }

    @Override
    protected void initInput() {
        onKeyDown(KeyCode.BACK_SPACE, () -> {
            if (output1.getText().isEmpty())
                return;

            output1.setText(output1.getText().substring(0, output1.getText().length() - 1));
        });

        onKeyDown(KeyCode.ENTER, () -> {
            takeUserInputAndGuess();
        });

        onKeyDown(KeyCode.SPACE, () -> {
            takeUserInputAndGuess();
        });

        getInput().addEventHandler(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().isEmpty())
                return;

            var ch = event.getCharacter().charAt(0);

            if (Character.isDigit(ch))
                output1.setText(output1.getText() + event.getCharacter());
        });
    }

    private void takeUserInputAndGuess() {
        var answer = output1.getText();

        player1.guess(answer);

        output1.setText("");
    }

    @Override
    protected void initGame() {
        levels = new ArrayList<>();
        levels.add(new LevelData(1, 30, List.of(Operation.ADD)));
        levels.add(new LevelData(5, 35, List.of(Operation.ADD, Operation.SUB)));

        currentLevel = levels.get(0);

        isAIReadyToGuess = true;

        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new MathrisFactory());

        player1 = spawn("player").getComponent(PlayerComponent.class);
        player2 = spawn("player").getComponent(PlayerComponent.class);

        for (int y = 0; y <= MAX_Y; y++) {

            // player 1
            for (int x = 0; x < 6; x++) {
                var block = spawnBlock(x, y);

                player1.addBlock(block);
            }

            // player 2
            for (int x = 7; x < 13; x++) {
                var block = spawnBlock(x, y);

                player2.addBlock(block);
            }
        }

        runOnce(() -> {

            // TODO: choice box that takes a list
            getDialogService().showChoiceBox("Select Difficulty", result -> {

                // TODO: allow selecting in Gameplay menu
                getSettings().setGameDifficulty(result);

                var aiData = AI_DATA.get(
                        getSettings().getGameDifficulty()
                );

                // start AI
                run(() -> {
                    onUpdateAI(aiData);
                }, Duration.seconds(aiData.guessInterval()));
            }, GameDifficulty.EASY, GameDifficulty.values());

        }, Duration.seconds(0.01));
    }

    private Entity spawnBlock(int x, int y) {
        return spawn("block",
                new SpawnData(40 + x * 120, y * 50)
                        .put("x", x)
                        .put("y", y)
                        .put("level", currentLevel)
        );
    }

    @Override
    protected void initUI() {
        output1 = getUIFactoryService().newText("", Color.WHITE, FontType.MONO, 22.0);
        output2 = getUIFactoryService().newText("", Color.WHITE, FontType.MONO, 22.0);

        addUINode(output1, 150, getAppHeight() - 70);
        addUINode(output2, 150 + 800, getAppHeight() - 70);

        var line = new Line(0, 0, 0, 20);
        line.setStroke(Color.WHITE);
        line.setStrokeWidth(5);

        addUINode(line, 130, getAppHeight() - 80);

        // UI

        var ui1 = new PlayerUI(player1);

        addUINode(ui1);

        var ui2 = new PlayerUI(player2);

        addUINode(ui2, 840, 0);

        for (int i = 0; i < 50; i++) {
            var length = getAppHeight() / 50;

            var separator = new Line(getAppWidth() / 2, i*length, getAppWidth() / 2, i*length + length);
            separator.setStrokeWidth(6);
            separator.setStrokeLineCap(StrokeLineCap.ROUND);
            separator.setStrokeLineJoin(StrokeLineJoin.BEVEL);
            separator.setStrokeType(StrokeType.CENTERED);
            separator.setStroke(Color.hsb(90, 0.8, 0.9, 0.9));

            animationBuilder()
                    .delay(Duration.seconds(i * 0.09))
                    .duration(Duration.seconds(0.5))
                    .autoReverse(true)
                    .repeatInfinitely()
                    .interpolator(Interpolators.BACK.EASE_OUT())
                    .scale(separator)
                    .to(new Point2D(1.9, 1.9))
                    .buildAndPlay();

            addUINode(separator);
        }
    }

    private void onUpdateAI(AIPlayerData data) {
        if (!isAIReadyToGuess)
            return;

        if (FXGLMath.randomBoolean(data.accuracy())) {
            byType(BLOCK)
                    .stream()
                    // bottom row of player1
                    .filter(e -> e.getInt("y") == MAX_Y && e.getInt("x") > 6)
                    .findAny()
                    .ifPresent(e -> {
                        isAIReadyToGuess = false;
                        animateAI(e.getString("answer"));
                    });
        } else {
            isAIReadyToGuess = false;
            animateAI("" + FXGLMath.random(1, 9999));
        }
    }

    private void animateAI(String guess) {
        for (int i = 0; i < guess.length(); i++) {
            final int index = i;

            runOnce(() -> {
                output2.setText(output2.getText() + guess.charAt(index));

                if (index + 1 == guess.length()) {
                    runOnce(() -> {
                        output2.setText("");
                        player2.guess(guess);
                        isAIReadyToGuess = true;
                    }, Duration.seconds(0.1));
                }

            }, Duration.seconds(i * 0.1));
        }
    }

    public void destroyBlock(Entity block) {
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

        } else {

            // drop down blocks above the one destroyed
            dropDown(blocks);
        }
    }

    // TODO: allow "sending" new blocks to enemy
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

    public PlayerComponent getOtherPlayer(PlayerComponent player) {
        return player == player1 ? player2 : player1;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
