package rubikscubesolvernew;

import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.PerspectiveCamera;
import javafx.scene.text.Font;
import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;

public class Cube extends StackPane {
    private static final Color[] FACE_COLORS = {
        Color.BLACK,  // value 0: internal (not visible)
        Color.WHITE,  // value 1: Up
        Color.RED,    // value 2: Right
        Color.GREEN,  // value 3: Front
        Color.YELLOW, // value 4: Down
        Color.ORANGE, // value 5: Left
        Color.BLUE    // value 6: Back
    };
    private Group cubeGroup;

    public Cube() {
        cubeGroup = new Group();
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-15);

        SubScene subScene = new SubScene(cubeGroup, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.GRAY);

        initMouseControl(cubeGroup, subScene);

        this.getChildren().add(subScene);
    }

    public void buildCube(int[] state) {
        cubeGroup.getChildren().clear();
        double offset = 1.05;

        for (int i = 0; i < 54; i++) {
            int face = i / 9;
            int row = (i % 9) / 3;
            int col = i % 9 % 3;
            int colorVal = state[i];

            // Create the sticker Box
            Box tile = new Box(0.95, 0.95, 0.05);
            PhongMaterial material = new PhongMaterial();
            
            // Bake the index ID string straight into the material texture map!
            material.setDiffuseMap(createFaceletTexture(FACE_COLORS[colorVal], String.valueOf(i)));
            tile.setMaterial(material);

            Translate translate = new Translate();
            Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
            Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

            switch (face) {
                case 0: // Up (White)
                    translate.setX((col - 1) * offset);
                    translate.setY(-1.5 * offset);
                    translate.setZ((1 - row) * offset);
                    rotateX.setAngle(-90);
                    break;
                    
                case 1: // Right (Red)
                    translate.setX(1.5 * offset);
                    translate.setY((row - 1) * offset);
                    translate.setZ((col - 1) * offset); // Inverted to match standard cube unwrapping
                    rotateY.setAngle(90);
                    break;
                    
                case 2: // Front (Green)
                    translate.setX((col - 1) * offset);
                    translate.setY((row - 1) * offset);
                    translate.setZ(-1.5 * offset);
                    break;
                    
                case 3: // Down (Yellow)
                    translate.setX((col - 1) * offset);
                    translate.setY(1.5 * offset);
                    translate.setZ((row - 1) * offset); // Inverted to keep orientation consistent
                    rotateX.setAngle(90);
                    break;
                    
                case 4: // Left (Orange)
                    translate.setX(-1.5 * offset);
                    translate.setY((row - 1) * offset);
                    translate.setZ((1 - col) * offset);
                    rotateY.setAngle(90);
                    break;
                    
                case 5: // Back (Blue)
                    translate.setX((1 - col) * offset); // Inverted so left-to-right matches up correctly
                    translate.setY((row - 1) * offset);
                    translate.setZ(1.5 * offset);
                    rotateY.setAngle(180);
                    break;
                    
                default:
                    break;
            }

            tile.getTransforms().addAll(translate, rotateX, rotateY);
            cubeGroup.getChildren().add(tile);
        }
    }

    private Image createFaceletTexture(Color faceColor, String text) {
        // Create a 128x128 2D layout canvas
        Canvas canvas = new Canvas(128, 128);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 1. Fill base sticker color
        gc.setFill(faceColor);
        gc.fillRect(0, 0, 128, 128);
        
        // 2. Optional: Draw a nice clean black border frame
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(6);
        gc.strokeRect(3, 3, 122, 122);
        
        // 3. Draw high-contrast centered text
        Color textColor = (faceColor == Color.WHITE || faceColor == Color.YELLOW) ? Color.BLACK : Color.WHITE;
        gc.setFill(textColor);
        gc.setFont(Font.font("Arial", 36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, 64, 64);
        
        // Take a snapshot to convert it into a 3D-friendly texture image
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    private void initMouseControl(Group group, SubScene scene) {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        group.getTransforms().addAll(xRotate, yRotate);

        final double[] anchorAngle = new double[2];
        final double[] anchorPos = new double[2];

        scene.setOnMousePressed(event -> {
            anchorPos[0] = event.getSceneX();
            anchorPos[1] = event.getSceneY();
            anchorAngle[0] = xRotate.getAngle();
            anchorAngle[1] = yRotate.getAngle();
        });

        scene.setOnMouseDragged(event -> {
            xRotate.setAngle(anchorAngle[0] - (anchorPos[1] - event.getSceneY()));
            yRotate.setAngle(anchorAngle[1] + (anchorPos[0] - event.getSceneX()));
        });
    }
}