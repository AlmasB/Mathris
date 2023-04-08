package com.almasb.mathris;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import static com.almasb.mathris.Config.MAX_Y;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class PlayerUI extends Parent {

    public PlayerUI(PlayerComponent player) {
        var bgTopLeft = new Rectangle(40, (MAX_Y+1)*50, Color.TRANSPARENT);
        bgTopLeft.setArcWidth(15);
        bgTopLeft.setArcHeight(15);
        bgTopLeft.setStrokeWidth(6);
        bgTopLeft.setStroke(Color.AQUAMARINE);
        bgTopLeft.setStrokeType(StrokeType.CENTERED);

        var bgMid = new Rectangle(120*6, (MAX_Y+1)*50, Color.TRANSPARENT);
        bgMid.setArcWidth(10);
        bgMid.setArcHeight(10);
        bgMid.setStroke(Color.AQUAMARINE);
        bgMid.setStrokeType(StrokeType.CENTERED);
        bgMid.setStrokeWidth(6);
        bgMid.setTranslateX(bgTopLeft.getWidth());

        var streakFill = new Rectangle(bgTopLeft.getWidth() - bgTopLeft.getStrokeWidth(), bgTopLeft.getHeight(), Color.ORANGERED);
        streakFill.heightProperty().bind(player.streakProperty().multiply(0.1 * bgTopLeft.getHeight()));
        streakFill.translateYProperty().bind(bgTopLeft.heightProperty().subtract(streakFill.heightProperty()));

        var streakShadowLeft = new Rectangle(bgTopLeft.getStrokeWidth(), bgTopLeft.getHeight(), Color.ORANGERED.brighter());
        streakShadowLeft.setTranslateX(5);
        streakShadowLeft.translateYProperty().bind(streakFill.translateYProperty());
        streakShadowLeft.heightProperty().bind(streakFill.heightProperty());

        getChildren().addAll(streakFill, streakShadowLeft, bgTopLeft, bgMid);
    }
}
