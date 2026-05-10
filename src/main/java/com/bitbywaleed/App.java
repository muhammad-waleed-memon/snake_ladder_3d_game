package com.bitbywaleed;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class App extends Application {

    private static final int TILE_SIZE = 80;
    private static final double BOARD_HALF = (TILE_SIZE * 10) / 2.0;

    private final Map<Integer, Point3D> tileMap = new HashMap<>();
    private final Map<Integer, BoardTile> tiles = new HashMap<>();
    private final Map<Integer, Integer> portals = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private final Random random = new Random();

    private Group world = new Group();
    private Dice3D dice;
    private int currentPlayer = 0;
    private boolean rolling = false;
    private boolean gameStarted = false;

    private Label turnLabel, statusLabel, instrLabel;
    private VBox menu, hud;
    private StackPane dicePane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        initPortals();
        createBoard();
        createLighting();

        // Camera - 3 steps back (was -1100, now -1400)
        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.setNearClip(0.1);
        cam.setFarClip(5000);
        cam.setTranslateY(-900);
        cam.setTranslateZ(-1400);  // CHANGED: moved back 300 units
        cam.setRotationAxis(Rotate.X_AXIS);
        cam.setRotate(-30);

        SubScene scene3D = new SubScene(world, 1280, 800, true, SceneAntialiasing.BALANCED);
        scene3D.setFill(Color.web("#050510"));
        scene3D.setCamera(cam);

        createMenu();
        createHUD();
        createDice();

        // Layer: 3D scene, dice (top right), HUD, menu
        StackPane root = new StackPane(scene3D, dicePane, hud, menu);

        scene3D.widthProperty().bind(root.widthProperty());
        scene3D.heightProperty().bind(root.heightProperty());

        Scene scene = new Scene(root, 1280, 800);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE && gameStarted && !rolling) {
                roll();
            }
        });

        stage.setTitle("Snakes & Ladders 3D");
        stage.setScene(scene);
        stage.show();
    }

    private void initPortals() {
        // Ladders
        portals.put(3, 22);
        portals.put(5, 14);
        portals.put(18, 44);
        portals.put(28, 84);
        portals.put(40, 62);
        portals.put(51, 67);
        portals.put(71, 91);
        // Snakes
        portals.put(17, 7);
        portals.put(54, 34);
        portals.put(62, 19);
        portals.put(64, 60);
        portals.put(87, 24);
        portals.put(93, 73);
        portals.put(99, 10);
    }

    private void createBoard() {
        // Ground plate
        Box ground = new Box(BOARD_HALF * 2.5, 10, BOARD_HALF * 2.5);
        ground.setMaterial(new PhongMaterial(Color.web("#0a0a1a")));
        ground.setTranslateY(20);
        world.getChildren().add(ground);

        // Tiles
        for (int i = 0; i < 100; i++) {
            int row = i / 10;
            int col = (row % 2 == 0) ? (i % 10) : (9 - (i % 10));
            double x = (col * TILE_SIZE) - BOARD_HALF + TILE_SIZE/2;
            double z = (row * TILE_SIZE) - BOARD_HALF + TILE_SIZE/2;

            BoardTile tile = new BoardTile(i + 1, TILE_SIZE);
            tile.setTranslateX(x);
            tile.setTranslateZ(z);
            world.getChildren().add(tile);

            tileMap.put(i + 1, new Point3D(x, 0, z));
            tiles.put(i + 1, tile);
        }

        // Snakes and Ladders
        portals.forEach((s, e) -> {
            world.getChildren().add(new SnakeLadder(s, e, tileMap.get(s), tileMap.get(e)));
        });
    }

    private void createLighting() {
        // Ambient
        world.getChildren().add(new AmbientLight(Color.rgb(80, 80, 120)));

        // Directional light
        PointLight light = new PointLight(Color.rgb(255, 255, 255));
        light.setTranslateY(-1000);
        light.setTranslateX(300);
        light.setTranslateZ(-500);
        world.getChildren().add(light);

        // Fill light
        PointLight fill = new PointLight(Color.rgb(100, 100, 150));
        fill.setTranslateY(-800);
        fill.setTranslateX(-400);
        world.getChildren().add(fill);
    }

    private void createDice() {
        dicePane = new StackPane();
        dicePane.setAlignment(Pos.TOP_RIGHT);
        dicePane.setPadding(new Insets(50, 50, 0, 0));
        dicePane.setPickOnBounds(false);

        // Dice container with its own scene
        Group diceGroup = new Group();
        dice = new Dice3D(50);
        diceGroup.getChildren().add(dice);

        // Lights for dice
        AmbientLight al = new AmbientLight(Color.WHITE);
        diceGroup.getChildren().add(al);

        PointLight pl = new PointLight(Color.WHITE);
        pl.setTranslateZ(-150);
        pl.setTranslateY(-100);
        diceGroup.getChildren().add(pl);

        // SubScene for dice with transparent background
        SubScene diceScene = new SubScene(diceGroup, 150, 150, true, SceneAntialiasing.BALANCED);
        diceScene.setFill(Color.TRANSPARENT);

        PerspectiveCamera diceCam = new PerspectiveCamera(true);
        diceCam.setTranslateZ(-200);
        diceCam.setNearClip(0.1);
        diceCam.setFarClip(1000);
        diceScene.setCamera(diceCam);

        dicePane.getChildren().add(diceScene);
    }

    private void createMenu() {
        menu = new VBox(30);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: rgba(5,5,16,0.95);");

        Label title = new Label("SNAKES & LADDERS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        title.setTextFill(Color.web("#ff1744"));

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ff1744"));
        glow.setRadius(30);
        title.setEffect(glow);

        Label subtitle = new Label("3D Edition");
        subtitle.setFont(Font.font("Arial", 24));
        subtitle.setTextFill(Color.web("#64b5f6"));

        HBox playerBox = new HBox(15);
        playerBox.setAlignment(Pos.CENTER);
        Label plabel = new Label("Players:");
        plabel.setTextFill(Color.WHITE);
        plabel.setFont(Font.font(18));
        Spinner<Integer> countSpin = new Spinner<>(2, 4, 2);
        countSpin.setPrefWidth(80);
        playerBox.getChildren().addAll(plabel, countSpin);

        VBox nameBox = new VBox(10);
        nameBox.setAlignment(Pos.CENTER);
        TextField[] fields = new TextField[4];
        for (int i = 0; i < 4; i++) {
            fields[i] = new TextField("Player " + (i+1));
            fields[i].setMaxWidth(250);
            fields[i].setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-size: 14;");
            fields[i].setVisible(i < 2);
            fields[i].setManaged(i < 2);
            nameBox.getChildren().add(fields[i]);
        }

        countSpin.valueProperty().addListener((obs, o, n) -> {
            for (int i = 0; i < 4; i++) {
                boolean v = i < n;
                fields[i].setVisible(v);
                fields[i].setManaged(v);
            }
        });

        Button start = new Button("START GAME");
        start.setStyle("-fx-background-color: #ff1744; -fx-text-fill: white; -fx-font-size: 20; -fx-padding: 12 50; -fx-cursor: hand;");
        start.setOnMouseEntered(e -> start.setStyle("-fx-background-color: #f50057; -fx-text-fill: white; -fx-font-size: 20; -fx-padding: 12 50; -fx-cursor: hand;"));
        start.setOnMouseExited(e -> start.setStyle("-fx-background-color: #ff1744; -fx-text-fill: white; -fx-font-size: 20; -fx-padding: 12 50; -fx-cursor: hand;"));

        start.setOnAction(e -> {
            Color[] colors = {Color.web("#ff1744"), Color.web("#00e5ff"), Color.web("#76ff03"), Color.web("#ffea00")};
            for (int i = 0; i < countSpin.getValue(); i++) {
                Player p = new Player(i+1, fields[i].getText(), colors[i]);
                players.add(p);
                world.getChildren().add(p);

                Point3D pos = tileMap.get(1);
                p.setTranslateX(pos.getX() + (i*25-37));
                p.setTranslateY(-30);
                p.setTranslateZ(pos.getZ());
            }
            gameStarted = true;
            menu.setVisible(false);
            hud.setVisible(true);
            updateTurn();
        });

        menu.getChildren().addAll(title, subtitle, playerBox, nameBox, start);
    }

    private void createHUD() {
        hud = new VBox(20);
        hud.setPadding(new Insets(40));
        hud.setVisible(false);

        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 42));

        statusLabel = new Label("Press SPACE to roll");
        statusLabel.setFont(Font.font(20));
        statusLabel.setTextFill(Color.web("#bbb"));

        instrLabel = new Label("[SPACE] ROLL");
        instrLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        instrLabel.setTextFill(Color.web("#76ff03"));

        FadeTransition ft = new FadeTransition(Duration.millis(600), instrLabel);
        ft.setFromValue(1);
        ft.setToValue(0.3);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();

        hud.getChildren().addAll(turnLabel, statusLabel, instrLabel);
    }

    private void roll() {
        rolling = true;
        instrLabel.setVisible(false);
        statusLabel.setText("Rolling...");

        int roll = random.nextInt(6) + 1;
        dice.roll(roll, () -> {
            statusLabel.setText("Rolled: " + roll);
            movePlayer(roll);
        });
    }

    private void movePlayer(int roll) {
        Player p = players.get(currentPlayer);
        int from = p.getCurrentTile();
        int to = Math.min(from + roll, 100);

        if (from + roll > 100) {
            statusLabel.setText("Need exact roll to finish!");
            nextTurn();
            return;
        }

        p.moveToTile(to, tileMap, () -> {
            if (portals.containsKey(to)) {
                int dest = portals.get(to);
                boolean up = dest > to;
                statusLabel.setText(up ? "Ladder! Climbing up..." : "Snake! Sliding down...");
                p.slideToTile(dest, tileMap, this::checkWin);
            } else {
                checkWin();
            }
        });
    }

    private void checkWin() {
        if (players.get(currentPlayer).getCurrentTile() == 100) {
            statusLabel.setText(players.get(currentPlayer).getName() + " WINS!");
            turnLabel.setText("🎉 WINNER 🎉");
            instrLabel.setText("GAME OVER");
            instrLabel.setTextFill(Color.web("#ff1744"));
            instrLabel.setVisible(true);
            return;
        }
        nextTurn();
    }

    private void nextTurn() {
        currentPlayer = (currentPlayer + 1) % players.size();
        rolling = false;
        instrLabel.setVisible(true);
        updateTurn();
    }

    private void updateTurn() {
        Player p = players.get(currentPlayer);
        turnLabel.setText(p.getName());
        turnLabel.setTextFill(p.getColor());

        // Tile highlight
        tiles.values().forEach(t -> t.setActive(false));
        tiles.get(p.getCurrentTile()).setActive(true);
    }
}