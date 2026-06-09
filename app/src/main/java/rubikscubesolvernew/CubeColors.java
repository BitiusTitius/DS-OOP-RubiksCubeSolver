package rubikscubesolvernew;

import javafx.scene.paint.Color;

public final class CubeColors {
    public static final String[] NAMES = {
        "", "White", "Red", "Green", "Yellow", "Orange", "Blue"
    };

    private static final Color[] FX_COLORS = {
        Color.BLACK,
        Color.WHITE,
        Color.RED,
        Color.GREEN,
        Color.YELLOW,
        Color.ORANGE,
        Color.BLUE
    };

    private CubeColors() {
    }

    public static String name(int color) {
        if (color < 1 || color > 6) {
            return "Unknown";
        }
        return NAMES[color];
    }

    public static Color fxColor(int color) {
        if (color < 0 || color > 6) {
            return Color.BLACK;
        }
        return FX_COLORS[color];
    }

    public static int nextColor(int color) {
        return color >= 6 ? 1 : color + 1;
    }
}
