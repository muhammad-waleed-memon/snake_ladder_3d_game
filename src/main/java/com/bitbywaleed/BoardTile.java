package com.bitbywaleed;


import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

public class BoardTile extends Group {
    private final int tileNumber;
    private final Box glowBox;
    private static final Color DEFAULT_COLOR = Color.web("#1a237e");  // Dark blue
    private static final Color ACTIVE_COLOR = Color.web("#ff1744");    // Red

    public BoardTile(int id, double size) {
        this.tileNumber = id;

        // Dark base
        Box base = new Box(size - 4, 6, size - 4);
        base.setMaterial(new PhongMaterial(Color.web("#0d1642")));
        base.setTranslateY(3);

        // Colored top
        glowBox = new Box(size - 20, 4, size - 20);
        PhongMaterial mat = new PhongMaterial(DEFAULT_COLOR);
        mat.setSpecularColor(Color.WHITE);
        mat.setSpecularPower(32);
        glowBox.setMaterial(mat);
        glowBox.setTranslateY(-2);

        // Number
        Text text = new Text(String.valueOf(id));
        text.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        text.setTranslateX(id < 10 ? -5 : (id < 100 ? -10 : -15));
        text.setTranslateY(-8);
        text.setTranslateZ(size/2 - 10);
        text.setRotationAxis(Rotate.X_AXIS);
        text.setRotate(-90);

        getChildren().addAll(base, glowBox, text);
    }

    public void setActive(boolean active) {
        ((PhongMaterial) glowBox.getMaterial()).setDiffuseColor(active ? ACTIVE_COLOR : DEFAULT_COLOR);
    }

    public int getTileNumber() {
        return tileNumber;
    }
}