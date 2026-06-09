package rubikscubesolvernew;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class GUIStuff {
    private static Timeline statusTimeline;
    private static final String DEFAULT_STATUS = "Left click for clockwise, right click for counterclockwise";

    static void setTemporaryStatus(String message, int seconds, Label statusLabel) {
        if (statusTimeline != null) {
            statusTimeline.stop();
        }

        statusLabel.setText(message);
        statusTimeline = new Timeline(new KeyFrame(Duration.seconds(seconds), e -> {
            statusLabel.setText(DEFAULT_STATUS);
        }));
        statusTimeline.play();
    }
}
