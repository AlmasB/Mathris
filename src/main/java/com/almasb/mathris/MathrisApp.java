package com.almasb.mathris;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.gameplay.GameDifficulty;
import com.almasb.fxgl.ui.FontType;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private int currentLevelIndex;
    private boolean hasLevelStarted;

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

        onKeyDown(KeyCode.F, () -> {
            if (player1.getStreak() > 5) {
                var numBlocks = player1.getStreak() / 3;

                player1.clearStreak();

                for (int i = 0; i < numBlocks; i++) {
                    player2.applyNegativeEffect(NegativeEffect.EXTRA_BLOCKS);
                }
            }
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

        // DEBUG
        if (!isReleaseMode()) {
            onKeyDown(KeyCode.L, () -> {
                nextLevel();
            });

            onKeyDown(KeyCode.K, () -> {
                player1.applyNegativeEffect(NegativeEffect.EXTRA_BLOCKS);
            });
        }
    }

    private void takeUserInputAndGuess() {
        if (!hasLevelStarted)
            return;

        var answer = output1.getText();

        player1.guess(answer);

        output1.setText("");
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("levelTime", 0.0);
    }

    @Override
    protected void initGame() {
        initLevels();

        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new MathrisFactory());

        player1 = spawn("player", new SpawnData().put("isPlayer1", true)).getComponent(PlayerComponent.class);
        player2 = spawn("player").getComponent(PlayerComponent.class);

        initAI();
    }

    private void initAI() {
        runOnce(() -> {

            // TODO: choice box that takes a list
            getDialogService().showChoiceBox("Select Difficulty", result -> {
                nextLevel();

                // TODO: allow selecting in Gameplay menu
                getSettings().setGameDifficulty(result);

                var aiData = AI_DATA.get(
                        getSettings().getGameDifficulty()
                );

                // start AI
                run(() -> {
                    onUpdateAI(aiData);
                }, Duration.seconds(aiData.guessInterval()));

                isAIReadyToGuess = true;
            }, GameDifficulty.EASY, GameDifficulty.values());

        }, Duration.seconds(0.01));
    }

    private void initLevels() {
        levels = new ArrayList<>();
        levels.add(new LevelData(1, 30, List.of(Operation.ADD)));
        levels.add(new LevelData(5, 35, List.of(Operation.ADD, Operation.SUB)));
        levels.add(new LevelData(1, 45, List.of(Operation.ADD, Operation.SUB, Operation.MUL)));
        levels.add(new LevelData(1, 60, List.of(Operation.ADD, Operation.SUB, Operation.MUL, Operation.DIV)));
        levels.add(new LevelData(1, 75, List.of(Operation.ADD, Operation.SUB, Operation.MUL, Operation.DIV, Operation.POW)));
        levels.add(new LevelData(1, 89, List.of(Operation.ADD, Operation.SUB, Operation.MUL, Operation.DIV, Operation.POW, Operation.MOD)));
        levels.add(new LevelData(11, 99, List.of(Operation.ADD, Operation.SUB, Operation.MUL, Operation.DIV, Operation.POW, Operation.MOD)));

        currentLevelIndex = -1;
        hasLevelStarted = false;
    }

    private void nextLevel() {
        if (currentLevelIndex >= 0) {
            var endLevelMessage = String.format("Level completed in: %.2fsec", getd("levelTime"));

            showMessage(endLevelMessage, () -> {
                getGameController().gotoLoading(() -> {
                    startLevel();
                });
            });
        } else {
            getGameController().gotoLoading(() -> {
                startLevel();
            });
        }
    }

    private void startLevel() {
        set("levelTime", 0.0);
        hasLevelStarted = false;

        player1.reset();
        player2.reset();

        if (currentLevelIndex < levels.size() - 1) {
            currentLevel = levels.get(++currentLevelIndex);

            for (int y = 0; y <= MAX_Y; y++) {

                // player 1 columns [0..5]
                for (int x = 0; x < 6; x++) {
                    var block = createBlock(x, y);

                    player1.addBlock(block);
                }

                // player 2 columns [7..12]
                for (int x = 7; x < 13; x++) {
                    var block = createBlock(x, y);

                    player2.addBlock(block);
                }
            }

            var entities = new ArrayList<Entity>();
            entities.addAll(player1.getBlocks());
            entities.addAll(player2.getBlocks());

            var level = new Level(getAppWidth(), getAppHeight(), entities);

            getGameWorld().setLevel(level);

            animateNextLevel();
        } else {
            runOnce(() -> showMessage("Demo Over!", getGameController()::gotoMainMenu), Duration.seconds(0.01));
        }
    }

    public Entity createBlock(int x, int y) {
        return getGameWorld().create("block",
                new SpawnData(40 + x * 120, y * 50)
                        .put("x", x)
                        .put("y", y)
                        .put("level", currentLevel)
        );
    }

    private void animateNextLevel() {
        // World
        var blocks = byType(BLOCK);

        for (int i = 0; i < blocks.size(); i++) {
            var block = blocks.get(i);

            animationBuilder()
                    .delay(Duration.millis(50 + block.getY() / getAppHeight() * 650))
                    .duration(Duration.seconds(0.9))
                    .interpolator(Interpolators.ELASTIC.EASE_OUT())
                    .scale(block)
                    .origin(new Point2D(60, 25))
                    .from(new Point2D(0, 0))
                    .to(new Point2D(1, 1))
                    .buildAndPlay();
        }

        // UI
        var text = getUIFactoryService().newText("Level " + (currentLevelIndex+1), 94);
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(5);
        text.setCache(true);
        text.setCacheHint(CacheHint.SPEED);

        addUINode(text);
        centerTextX(text, 0.0, getAppWidth());
        centerTextY(text, 0.0, 400.0);

        animationBuilder()
                .delay(Duration.seconds(1))
                .duration(Duration.seconds(1.6))
                .onFinished(() -> {
                    removeUINode(text);
                    hasLevelStarted = true;
                })
                .interpolator(Interpolators.EXPONENTIAL.EASE_IN())
                .translate(text)
                .to(new Point2D(text.getTranslateX(), getAppHeight() * 1.2))
                .buildAndPlay();
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

    @Override
    protected void onUpdate(double tpf) {
        inc("levelTime", tpf);
    }

    private void onUpdateAI(AIPlayerData data) {
        if (!hasLevelStarted)
            return;

        if (!isAIReadyToGuess)
            return;

        var accuracy = data.accuracy();

        if (player2.hasEffect(PlayerComponent.HideEffect.class)) {
            accuracy -= 0.45;
        }

        if (player2.hasEffect(PlayerComponent.BigNumbersEffect.class)) {
            accuracy -= 0.2;
        }

        accuracy = Math.max(accuracy, 0.0);

        if (FXGLMath.randomBoolean(accuracy)) {
            player2.getBlocks()
                    .stream()
                    .filter(PlayerComponent::isBottomRow)
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

    public void onPlayerWon(PlayerComponent player) {
        if (!hasLevelStarted)
            return;

        if (player == player1) {
            nextLevel();
        } else {
            showMessage("You lost!", getGameController()::gotoMainMenu);
        }
    }

    public PlayerComponent getOtherPlayer(PlayerComponent player) {
        return player == player1 ? player2 : player1;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
