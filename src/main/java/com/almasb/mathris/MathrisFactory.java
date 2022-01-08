package com.almasb.mathris;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.ui.FontType;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.mathris.Config.MAX_Y;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class MathrisFactory implements EntityFactory {

    @Spawns("block")
    public Entity newBlock(SpawnData data) {
        String question = data.get("question");
        Color color = data.get("color");

        var sizeW = 120;
        var sizeH = 50;
        var offset = 5;

        var bg = new Rectangle(sizeW, sizeH, Color.LIGHTGRAY);
        bg.setArcWidth(5);
        bg.setArcHeight(5);
        bg.setStroke(Color.BLACK);
        bg.setStrokeType(StrokeType.INSIDE);

        var polyLeft = new Polygon(
                0.0, 0.0, offset, offset,
                offset, sizeH - offset, 0.0, sizeH
        );
        polyLeft.setFill(color.brighter());

        var polyRight = new Polygon(
                sizeW, 0.0, sizeW, sizeH,
                sizeW - offset, sizeH - offset, sizeW - offset, offset
        );
        polyRight.setFill(color.darker());

        var polyUp = new Polygon(
                0.0, 0.0, sizeW, 0.0,
                sizeW - offset, offset, offset, offset
        );
        polyUp.setFill(color.brighter().brighter());

        var polyDown = new Polygon(
                0.0, sizeH, offset, sizeH - offset,
                sizeW - offset, sizeH - offset, sizeW, sizeH
        );
        polyDown.setFill(color.darker().darker());

        var text = getUIFactoryService().newText(question, Color.BLACK, FontType.MONO, sizeH *0.5);

        var overlay = new Rectangle(sizeW, sizeH, Color.TRANSPARENT);
        overlay.setArcWidth(5);
        overlay.setArcHeight(5);
        overlay.setStrokeWidth(3);
        overlay.setStrokeType(StrokeType.INSIDE);

        if (FXGLMath.randomBoolean(0.12)) {
            overlay.setStroke(Color.YELLOW);

//            if (FXGLMath.randomBoolean(0.3)) {
//                overlay.setStroke(Color.DARKSEAGREEN);
//            }
//
//            if (FXGLMath.randomBoolean(0.3)) {
//                overlay.setStroke(Color.BLUE);
//            }
        }

        var stack = new StackPane(new Pane(bg, polyLeft, polyRight, polyUp, polyDown), text);

        var block = entityBuilder(data)
                .type(EntityType.BLOCK)
                .view(stack)
                .view(overlay)
                .build();

        // TODO: extract max Y as Config

        stack.opacityProperty().bind(
                Bindings.when(block.getProperties().intProperty("y").isEqualTo(MAX_Y))
                        .then(1)
                        .otherwise(0.7)
        );

        return block;
    }
}
