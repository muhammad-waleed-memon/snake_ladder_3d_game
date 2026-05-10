package com.bitbywaleed;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class SnakeLadder extends Group {

    public SnakeLadder(int start, int end, Point3D s, Point3D e) {
        if (end > start) {
            createLadder(s, e);
        } else {
            createSnake(s, e);
        }
    }

    private void createLadder(Point3D s, Point3D e) {
        // Side rails
        double railOffset = 15;
        getChildren().add(createRail(s.add(-railOffset, 0, 0), e.add(-railOffset, 0, 0), Color.SADDLEBROWN, 3));
        getChildren().add(createRail(s.add(railOffset, 0, 0), e.add(railOffset, 0, 0), Color.SADDLEBROWN, 3));

        // Rungs
        int steps = Math.max(3, (int)(s.distance(e) / 30));
        for (int i = 1; i < steps; i++) {
            double t = (double)i / steps;
            Point3D pos = s.add(e.subtract(s).multiply(t));
            getChildren().add(createRail(pos.add(-railOffset, -8, 0), pos.add(railOffset, -8, 0), Color.PERU, 2.5));
        }
    }

    private Cylinder createRail(Point3D start, Point3D end, Color color, double radius) {
        double height = start.distance(end);
        Cylinder c = new Cylinder(radius, height);
        c.setMaterial(new PhongMaterial(color));

        Point3D mid = start.add(end).multiply(0.5);
        c.setTranslateX(mid.getX());
        c.setTranslateY(mid.getY() - 15);
        c.setTranslateZ(mid.getZ());

        // Orient
        Point3D diff = end.subtract(start);
        double angle = Math.toDegrees(Math.acos(diff.normalize().dotProduct(new Point3D(0, 1, 0))));
        Point3D axis = diff.crossProduct(new Point3D(0, 1, 0)).normalize();
        Rotate rot = new Rotate(-angle, axis);
        c.getTransforms().add(rot);

        return c;
    }

    private void createSnake(Point3D s, Point3D e) {
        int segments = 30;
        Sphere[] spheres = new Sphere[segments];
        double[] baseX = new double[segments];
        double[] baseY = new double[segments];
        double[] baseZ = new double[segments];

        // Create snake body
        for (int i = 0; i < segments; i++) {
            double t = (double)i / (segments - 1);

            // Size: big head to small tail
            double size = (i == 0) ? 20 : Math.max(8, 16 - t * 12);

            Sphere seg = new Sphere(size);
            Color segColor = (i == 0) ? Color.web("#d32f2f") : Color.web("#388e3c");
            seg.setMaterial(new PhongMaterial(segColor));

            // Position with sine wave
            double x = s.getX() + (e.getX() - s.getX()) * t;
            double z = s.getZ() + (e.getZ() - s.getZ()) * t;
            double y = -25 - Math.sin(t * Math.PI * 3) * 20;

            seg.setTranslateX(x);
            seg.setTranslateY(y);
            seg.setTranslateZ(z);

            spheres[i] = seg;
            baseX[i] = x;
            baseY[i] = y;
            baseZ[i] = z;

            getChildren().add(seg);
        }

        // Animate snake movement
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double time = now / 1e9;
                for (int i = 0; i < segments; i++) {
                    double t = (double)i / segments;
                    // Sine wave movement
                    double waveX = Math.sin(time * 8 + i * 0.4) * 8;
                    double waveY = Math.cos(time * 6 + i * 0.3) * 4;

                    spheres[i].setTranslateX(baseX[i] + waveX);
                    spheres[i].setTranslateY(baseY[i] + waveY);
                }
            }
        }.start();
    }
}