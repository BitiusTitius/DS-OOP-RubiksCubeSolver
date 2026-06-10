package rubikscubesolvernew;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class CubeStateEditorDialog {
    private static final int STICKER_SIZE = 36;
    private static final int GRID_ROWS = 10;
    private static final int GRID_COLS = 12;

    private final Stage stage;
    private final RubiksCube cube;
    private final int[] editorState;
    private final Button[][] stickerButtons = new Button[GRID_ROWS][GRID_COLS];
    private final Label errorLabel = new Label();
    
    private int selectedColor = 1;
    private final Button[] colorSwatches = new Button[7];

    public CubeStateEditorDialog(Window owner, RubiksCube cube) {
        this.cube = cube;
        this.editorState = cube.getState();
        this.stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Enter Cube State");
        stage.setScene(new Scene(buildContent(), 560, 600));
    }

    public void show() {
        stage.showAndWait();
    }

    private BorderPane buildContent() {
        GridPane net = buildNetGrid();
        VBox legend = buildLegend();
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        errorLabel.setWrapText(true);

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> confirm());

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> stage.close());

        HBox actions = new HBox(10, okButton, cancelButton);
        actions.setAlignment(Pos.CENTER);
        BorderPane.setMargin(actions, new Insets(12, 0, 0, 0));

        Label hint = new Label("Select a color from the palette below, then click a tile to paint it.");
        hint.setStyle("-fx-text-fill: #cccccc;");

        VBox upperContent = new VBox(12, hint, net, legend, errorLabel);
        upperContent.setAlignment(Pos.TOP_CENTER);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        root.setCenter(upperContent);
        root.setBottom(actions);
        
        return root;
    }

    private GridPane buildNetGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        for (int col = 0; col < GRID_COLS; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints(STICKER_SIZE);
            colConstraints.setHgrow(Priority.NEVER);
            grid.getColumnConstraints().add(colConstraints);
        }
        for (int row = 0; row < GRID_ROWS; row++) {
            RowConstraints rowConstraints = new RowConstraints(STICKER_SIZE);
            rowConstraints.setVgrow(Priority.NEVER);
            grid.getRowConstraints().add(rowConstraints);
        }

        placeFace(grid, 1, 3, 0, "U");
        placeFace(grid, 4, 0, 4, "L");
        placeFace(grid, 4, 3, 2, "F");
        placeFace(grid, 4, 6, 1, "R");
        placeFace(grid, 4, 9, 5, "B");
        placeFace(grid, 7, 3, 3, "D");

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (stickerButtons[row][col] == null) {
                    grid.add(new Region(), col, row);
                }
            }
        }

        return grid;
    }

    private void placeFace(GridPane grid, int gridRow, int gridCol, int face, String label) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int stickerIndex = face * 9 + row * 3 + col;
                Button sticker = createStickerButton(stickerIndex);
                
                if (row == 1 && col == 1) {
                    sticker.setText(label);
                }
                
                stickerButtons[gridRow + row][gridCol + col] = sticker;
                grid.add(sticker, gridCol + col, gridRow + row);
            }
        }
    }

    private Button createStickerButton(int stickerIndex) {
        Button button = new Button();
        button.setMinSize(STICKER_SIZE, STICKER_SIZE);
        button.setMaxSize(STICKER_SIZE, STICKER_SIZE);
        updateStickerAppearance(button, editorState[stickerIndex]);
        
        button.setOnAction(e -> {
            editorState[stickerIndex] = selectedColor;
            updateStickerAppearance(button, editorState[stickerIndex]);
            errorLabel.setText("");
        });
        return button;
    }

    private void updateStickerAppearance(Button button, int color) {
        String hex = CubeColors.fxColor(color).toString().replace("0x", "#");
        String textColor = (color == 1 || color == 4) ? "black" : "white";
        button.setStyle(
            "-fx-background-color: " + hex + ";" +
            "-fx-border-color: #222222;" +
            "-fx-border-width: 1px;" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-weight: bold;"
        );
    }

    private VBox buildLegend() {
        HBox swatches = new HBox(8);
        swatches.setAlignment(Pos.CENTER);
        for (int color = 1; color <= 6; color++) {
            Button swatch = new Button(CubeColors.name(color));
            colorSwatches[color] = swatch;
            
            final int currentColor = color;
            swatch.setOnAction(e -> {
                selectedColor = currentColor;
                updatePaletteHighlight();
            });
            
            swatches.getChildren().add(swatch);
        }
        
        updatePaletteHighlight();

        Label legendLabel = new Label("Color Palette");
        legendLabel.setStyle("-fx-text-fill: #cccccc;");
        VBox legend = new VBox(4, legendLabel, swatches);
        legend.setAlignment(Pos.CENTER);
        return legend;
    }

    private void updatePaletteHighlight() {
        for (int color = 1; color <= 6; color++) {
            Button swatch = colorSwatches[color];
            if (swatch != null) {
                String hex = CubeColors.fxColor(color).toString().replace("0x", "#");
                String textColor = (color == 1 || color == 4) ? "black" : "white";
                String border = (color == selectedColor) 
                    ? "-fx-border-color: #ffffff; -fx-border-width: 2px;" 
                    : "-fx-border-color: #222222; -fx-border-width: 1px;";
                swatch.setStyle(
                    "-fx-background-color: " + hex + ";" +
                    "-fx-text-fill: " + textColor + ";" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    border
                );
            }
        }
    }

    private void confirm() {
        CubeStateValidator.ValidationResult result = CubeStateValidator.validate(editorState);
        if (!result.isValid()) {
            errorLabel.setText(result.message());
            return;
        }
        cube.setState(editorState);
        stage.close();
    }
}