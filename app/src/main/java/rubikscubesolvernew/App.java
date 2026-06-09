package rubikscubesolvernew;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        RubiksCube cube = new RubiksCube();

        BorderPane root = new BorderPane();
        root.setCenter(cube.getCube3D());

        String defaultLabel = "Left click for clockwise, right click for counterclockwise";

        Label statusLabel = new Label(defaultLabel);
        statusLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        FlowPane buttonPanel = new FlowPane();
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setHgap(10);
        buttonPanel.setVgap(10);
        buttonPanel.setStyle("-fx-padding: 20; -fx-background-color: #333333;");

        Button addCube = new Button("Add cube");
        addCube.setStyle("-fx-font-size: 14px; -fx-min-width: 50px;");

        Button undoBtn = new Button("⮌");
        undoBtn.setStyle("-fx-font-size: 14px; -fx-min-width: 50px;");
        undoBtn.setDisable(true);

        Button redoBtn = new Button("⮎");
        redoBtn.setStyle("-fx-font-size: 14px; -fx-min-width: 50px;");
        redoBtn.setDisable(true);

        Runnable updateUndoRedoButtons = () -> {
            undoBtn.setDisable(!cube.canUndo());
            redoBtn.setDisable(!cube.canRedo());
        };

        addCube.setOnAction(e -> {
            new CubeStateEditorDialog(primaryStage, cube).show();
            updateUndoRedoButtons.run();
            statusLabel.setText("Cube state updated");
        });

        undoBtn.setOnAction(e -> {
            cube.undo();
            updateUndoRedoButtons.run();
            statusLabel.setText(cube.canUndo() ? "Undone" : "Nothing to undo");
        });

        redoBtn.setOnAction(e -> {
            cube.redo();
            updateUndoRedoButtons.run();
            statusLabel.setText(cube.canRedo() ? "Redone" : "Nothing to redo");
        });

        buttonPanel.getChildren().addAll(addCube, undoBtn);

        String[] notations = { "U", "R", "F", "D", "L", "B" };

        for (String n : notations) {
            Button button = new Button(n);
            button.setStyle("-fx-font-size: 14px; -fx-min-width: 50px;");
            button.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    cube.rotate(n);
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    cube.rotate(n + "'");
                }
                updateUndoRedoButtons.run();
            });
            buttonPanel.getChildren().add(button);
        }

        buttonPanel.getChildren().add(redoBtn);

        Button solveButton = new Button("Solve");
        solveButton.setStyle("-fx-font-size: 14px; -fx-min-width: 80px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        solveButton.setOnAction(e -> {
            solveButton.setDisable(true);
            statusLabel.setText("Calculating solution...");

            int[] state = cube.getState();
            Thread solverThread = new Thread(() -> {
                try {
                    String solution = new TwoPhaseSolver().solve(state);
                    Platform.runLater(() -> {
                        if (solution.isEmpty()) {
                            statusLabel.setText("Already solved");
                        } else {
                            cube.applyMoves(solution);
                            int moveCount = solution.trim().split("\\s+").length;
                            statusLabel.setText("Solved in " + moveCount + " moves");
                        }
                        updateUndoRedoButtons.run();
                        solveButton.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Solve failed: " + ex.getMessage());
                        solveButton.setDisable(false);
                    });
                }
            }, "cube-solver");
            solverThread.setDaemon(true);
            solverThread.start();
        });

        buttonPanel.getChildren().add(solveButton);

        FlowPane bottomPanel = new FlowPane();
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setStyle("-fx-padding: 5 20 10 20; -fx-background-color: #333333;");
        bottomPanel.getChildren().add(statusLabel);

        BorderPane bottomWrapper = new BorderPane();
        bottomWrapper.setTop(buttonPanel);
        bottomWrapper.setBottom(bottomPanel);

        root.setBottom(bottomWrapper);

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("3D Rubik's Cube Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}