package com.almasb.mathris;

import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.core.util.EmptyRunnable;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Almas Baim (https://github.com/AlmasB)
 */
public final class CodefestService extends EngineService {

    private static final List<String> KEYS = new ArrayList<>(
            List.of(
                    "Ae8kjghsnxcb63na",
                    "5ahjhasdh3hj767ghshasd",
                    "c8j3hayjydh3jfhgggx0pf86mha4",
                    "ui4iuh9k3jjurgadh4yygu5hpljyhbjik3hdtg"
            )
    );


    private Runnable callback = EmptyRunnable.INSTANCE;

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    public void check() {
        System.out.println("Checking");

        FXGL.getNetService().openStreamTask("https://github.com/AlmasB/Events/issues/2")
                .thenWrap(stream -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

                        String line = null;

                        while ((line = reader.readLine()) != null) {

                            if (KEYS.isEmpty()) {
                                break;
                            }

                            var key = KEYS.get(0);

                            if (line.contains(key)) {
                                System.out.println("Running callback");

                                callback.run();
                                KEYS.remove(0);
                                break;
                            }
                        }

                    } catch (Exception e) {
                        Logger.get(CodefestService.class).warning("Could not connect to server", e);
                    }

                    return "OK";
                })
                .onSuccess(output -> System.out.println(output))
                .run();
    }
}
