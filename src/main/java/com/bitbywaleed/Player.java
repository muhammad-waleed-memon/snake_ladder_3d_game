package com.bitbywaleed;

import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import java.util.Map;

public class Player extends Group {
    private final int playerId;
    private int currentTile = 1;
    private final String name;
    private final Color color;

    public Player(int id, String name, Color color) {
        this.playerId = id;
        this.name = name;
        this.color = color;

        // Main sphere
        Sphere sphere = new Sphere(18);
        PhongMaterial mat = new PhongMaterial(color);
        mat.setSpecularColor(Color.WHITE);
        mat.setSpecularPower(32);
        sphere.setMaterial(mat);

        getChildren().add(sphere);
    }

    public void moveToTile(int targetTile, Map<Integer, Point3D> tileMap, Runnable onDone) {
        SequentialTransition seq = new SequentialTransition();
        double offsetX = (playerId - 1) * 25 - 37;

        for (int i = currentTile + 1; i <= targetTile; i++) {
            Point3D pos = tileMap.get(i);

            TranslateTransition move = new TranslateTransition(Duration.millis(250), this);
            move.setToX(pos.getX() + offsetX);
            move.setToZ(pos.getZ());

            Timeline jump = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(translateYProperty(), -30)),
                    new KeyFrame(Duration.millis(125), new KeyValue(translateYProperty(), -90, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(250), new KeyValue(translateYProperty(), -30, Interpolator.EASE_IN))
            );

            ParallelTransition step = new ParallelTransition(move, jump);
            final int landed = i;
            step.setOnFinished(e -> currentTile = landed);
            seq.getChildren().add(step);
        }

        seq.setOnFinished(e -> { if (onDone != null) onDone.run(); });
        seq.play();
    }

    public void slideToTile(int targetTile, Map<Integer, Point3D> tileMap, Runnable onDone) {
        Point3D pos = tileMap.get(targetTile);
        double offsetX = (playerId - 1) * 25 - 37;

        TranslateTransition slide = new TranslateTransition(Duration.seconds(1.2), this);
        slide.setToX(pos.getX() + offsetX);
        slide.setToZ(pos.getZ());
        slide.setInterpolator(Interpolator.EASE_BOTH);

        // Bobbing animation during slide
        Timeline bob = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(translateYProperty(), -30)),
                new KeyFrame(Duration.millis(400), new KeyValue(translateYProperty(), -70)),
                new KeyFrame(Duration.millis(800), new KeyValue(translateYProperty(), -30)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(translateYProperty(), -50))
        );

        ParallelTransition pt = new ParallelTransition(slide, bob);
        pt.setOnFinished(e -> {
            currentTile = targetTile;
            setTranslateY(-30);
            if (onDone != null) onDone.run();
        });
        pt.play();
    }

    public int getCurrentTile() { return currentTile; }
    public String getName() { return name; }
    public Color getColor() { return color; }
    public int getPlayerId() { return playerId; }
}