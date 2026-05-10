package com.bitbywaleed;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Dice3D extends Group {
    private final Group diceGroup;
    private final Rotate rx = new Rotate(0, Rotate.X_AXIS);
    private final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
    private final double size;

    public Dice3D(double size) {
        this.size = size;
        this.diceGroup = new Group();

        // Create dice with realistic dots
        createDiceWithDots();

        diceGroup.getTransforms().addAll(rx, ry);
        getChildren().add(diceGroup);
    }

    private void createDiceWithDots() {
        // Main cube - white with slight bevel effect using specular
        Box cube = new Box(size, size, size);
        PhongMaterial mat = new PhongMaterial(Color.WHITE);
        mat.setSpecularColor(Color.LIGHTGRAY);
        mat.setSpecularPower(64);
        cube.setMaterial(mat);
        diceGroup.getChildren().add(cube);

        // Dot size and positions
        double dotRadius = size * 0.14;  // Slightly larger dots
        double offset = size * 0.22;     // Position offset from center

        // Face 1 (front, Z+) - 1 dot in center
        addFaceDots(new double[][] {{0, 0}}, dotRadius, 0, 0, size/2, 0, 0, 0);

        // Face 2 (right, X+) - 2 dots diagonal
        addFaceDots(new double[][] {{-offset, -offset}, {offset, offset}}, dotRadius, size/2, 0, 0, 0, 90, 0);

        // Face 3 (top, Y-) - 3 dots diagonal
        addFaceDots(new double[][] {{-offset, -offset}, {0, 0}, {offset, offset}}, dotRadius, 0, -size/2, 0, -90, 0, 0);

        // Face 4 (bottom, Y+) - 4 dots in corners
        addFaceDots(new double[][] {
                {-offset, -offset}, {offset, -offset},
                {-offset, offset}, {offset, offset}
        }, dotRadius, 0, size/2, 0, 90, 0, 0);

        // Face 5 (left, X-) - 5 dots (center + corners)
        addFaceDots(new double[][] {
                {-offset, -offset}, {offset, -offset},
                {0, 0},
                {-offset, offset}, {offset, offset}
        }, dotRadius, -size/2, 0, 0, 0, -90, 0);

        // Face 6 (back, Z-) - 6 dots
        addFaceDots(new double[][] {
                {-offset, -offset}, {offset, -offset},
                {-offset, 0}, {offset, 0},
                {-offset, offset}, {offset, offset}
        }, dotRadius, 0, 0, -size/2, 180, 0, 180); // Note: rotation to flip dots orientation
    }

    private void addFaceDots(double[][] positions, double dotRadius,
                             double tx, double ty, double tz,
                             double rotX, double rotY, double rotZ) {
        Group face = new Group();

        for (double[] pos : positions) {
            // Use Sphere for realistic circular dots
            Sphere dot = new Sphere(dotRadius);
            PhongMaterial dotMat = new PhongMaterial(Color.BLACK);
            dotMat.setSpecularColor(Color.DARKGRAY);  // Slight shine on dots
            dotMat.setSpecularPower(32);
            dot.setMaterial(dotMat);

            // Position on face plane (local coordinates)
            dot.setTranslateX(pos[0]);
            dot.setTranslateY(pos[1]);
            dot.setTranslateZ(0);

            face.getChildren().add(dot);
        }

        // Position the entire face
        face.setTranslateX(tx);
        face.setTranslateY(ty);
        face.setTranslateZ(tz);

        // Apply rotations in correct order (Z, then Y, then X)
        if (rotZ != 0) {
            face.setRotationAxis(Rotate.Z_AXIS);
            face.setRotate(rotZ);
        }

        if (rotY != 0) {
            Rotate yRot = new Rotate(rotY, Rotate.Y_AXIS);
            face.getTransforms().add(yRot);
        }

        if (rotX != 0) {
            Rotate xRot = new Rotate(rotX, Rotate.X_AXIS);
            face.getTransforms().add(xRot);
        }

        diceGroup.getChildren().add(face);
    }

    public void roll(int result, Runnable onFinished) {
        // Random spins - more realistic rolling
        double spinsX = 720 + Math.random() * 720;  // At least 2 full rotations
        double spinsY = 720 + Math.random() * 720;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(rx.angleProperty(), rx.getAngle()),
                        new KeyValue(ry.angleProperty(), ry.getAngle())),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(rx.angleProperty(), rx.getAngle() + spinsX),
                        new KeyValue(ry.angleProperty(), ry.getAngle() + spinsY))
        );

        timeline.setOnFinished(e -> {
            snapToResult(result);
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }

    private void snapToResult(int r) {
        // Precise angles to show correct face
        // Using standard dice orientation: opposite faces sum to 7
        // 1 opposite 6, 2 opposite 5, 3 opposite 4
        switch(r) {
            case 1: rx.setAngle(180); ry.setAngle(0); break;        // Front (1 dot)
            case 2: rx.setAngle(0); ry.setAngle(90); break;      // Right (2 dots)
            case 3: rx.setAngle(90); ry.setAngle(0); break;       // Top (3 dots)
            case 4: rx.setAngle(-90); ry.setAngle(0); break;      // Bottom (4 dots)
            case 5: rx.setAngle(0); ry.setAngle(-90); break;       // Left (5 dots)
            case 6: rx.setAngle(0); ry.setAngle(0); break;      // Back (6 dots)
        }
    }
}