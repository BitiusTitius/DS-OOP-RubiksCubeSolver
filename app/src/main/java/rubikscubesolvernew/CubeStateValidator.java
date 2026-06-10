package rubikscubesolvernew;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class CubeStateValidator {
    private static final int[] OPPOSITE = {0, 4, 5, 6, 1, 2, 3, 0, 0, 0, 0};
    private static final int[][] CENTER_PAIRS = {{4, 31}, {13, 40}, {22, 49}};
    private static final int[][] EDGE_PAIRS = {
        {1, 46},
        {5, 10},
        {7, 19},
        {3, 37},
        {28, 25},
        {32, 16},
        {34, 52},
        {30, 43},
        {23, 12},
        {21, 41},
        {48, 14},
        {50, 39}
    };

    private static final int[][] CORNER_TRIPLES = {
        {6, 18, 38},
        {8, 20, 9},
        {2, 45, 11},
        {0, 47, 36},
        {27, 24, 44},
        {29, 26, 15},
        {35, 51, 17},
        {33, 53, 42}
    };
    private static final Set<Set<Integer>> VALID_EDGES = Set.of(
        Set.of(1, 2), Set.of(1, 3), Set.of(1, 5), Set.of(1, 6),
        Set.of(4, 2), Set.of(4, 3), Set.of(4, 5), Set.of(4, 6),
        Set.of(2, 3), Set.of(2, 6), Set.of(3, 5), Set.of(5, 6)
    );
    private static final Set<Set<Integer>> VALID_CORNERS = Set.of(
        Set.of(1, 2, 3), Set.of(1, 3, 5), Set.of(1, 5, 6), Set.of(1, 2, 6),
        Set.of(4, 2, 3), Set.of(4, 3, 5), Set.of(4, 5, 6), Set.of(4, 2, 6)
    );

    private CubeStateValidator() {
    }

    public static ValidationResult validate(int[] state) {
        ValidationResult structural = validateStructure(state);
        if (!structural.isValid()) {
            return structural;
        }

        if (!TwoPhaseSolver.isReachable(state)) {
            return ValidationResult.invalid("Cube state is not physically reachable (invalid permutation or orientation).");
        }

        return ValidationResult.ok();
    }

    static ValidationResult validateStructure(int[] state) {
        if (state == null || state.length != 54) {
            return ValidationResult.invalid("Cube state must have exactly 54 stickers.");
        }

        ValidationResult colorCount = validateColorCounts(state);
        if (!colorCount.isValid()) {
            return colorCount;
        }

        ValidationResult centers = validateCenterOpposites(state);
        if (!centers.isValid()) {
            return centers;
        }

        ValidationResult edges = validateEdges(state);
        if (!edges.isValid()) {
            return edges;
        }

        ValidationResult corners = validateCorners(state);
        if (!corners.isValid()) {
            return corners;
        }

        return ValidationResult.ok();
    }

    private static ValidationResult validateColorCounts(int[] state) {
        int[] counts = new int[7];
        for (int color : state) {
            if (color < 1 || color > 6) {
                return ValidationResult.invalid("Invalid color value: " + color + ". Colors must be 1–6.");
            }
            counts[color]++;
        }
        for (int color = 1; color <= 6; color++) {
            if (counts[color] != 9) {
                return ValidationResult.invalid(
                    "Each color must appear exactly 9 times (color " + color + " appears " + counts[color] + " times).");
            }
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validateCenterOpposites(int[] state) {
        for (int[] pair : CENTER_PAIRS) {
            int a = state[pair[0]];
            int b = state[pair[1]];
            if (OPPOSITE[a] != b) {
                return ValidationResult.invalid(
                    "Opposite face centers must be paired correctly (e.g. white/yellow, red/orange, green/blue).");
            }
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validateEdges(int[] state) {
        for (int[] edge : EDGE_PAIRS) {
            int a = state[edge[0]];
            int b = state[edge[1]];
            if (a == b) {
                return ValidationResult.invalid("Invalid edge: an edge piece cannot have the same color on both sides.");
            }
            if (OPPOSITE[a] == b) {
                return ValidationResult.invalid("Invalid edge: opposite colors cannot appear on the same edge piece.");
            }
            Set<Integer> colors = Set.of(a, b);
            if (!VALID_EDGES.contains(colors)) {
                return ValidationResult.invalid(
                    "Invalid edge piece colors: " + CubeColors.name(a) + " and " + CubeColors.name(b) + ".");
            }
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validateCorners(int[] state) {
        for (int[] corner : CORNER_TRIPLES) {
            int a = state[corner[0]];
            int b = state[corner[1]];
            int c = state[corner[2]];
            if (a == b || a == c || b == c) {
                return ValidationResult.invalid("Invalid corner: a corner piece cannot repeat the same color.");
            }
            if (OPPOSITE[a] == b || OPPOSITE[a] == c || OPPOSITE[b] == c) {
                return ValidationResult.invalid("Invalid corner: opposite colors cannot appear on the same corner piece.");
            }
            Set<Integer> colors = new HashSet<>(Arrays.asList(a, b, c));
            if (!VALID_CORNERS.contains(colors)) {
                return ValidationResult.invalid(
                    "Invalid corner piece colors: " + CubeColors.name(a) + ", " + CubeColors.name(b) + ", and " + CubeColors.name(c) + ".");
            }
        }
        return ValidationResult.ok();
    }

    public record ValidationResult(boolean isValid, String message) {
        public static ValidationResult ok() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}
