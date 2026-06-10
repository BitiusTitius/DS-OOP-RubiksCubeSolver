package rubikscubesolvernew;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class RubiksCube {
    private int[] state = new int[54];
    private static final int[] SOLVED_STATE = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, // Up face (White)
        2, 2, 2, 2, 2, 2, 2, 2, 2, // Right face (Red)
        3, 3, 3, 3, 3, 3, 3, 3, 3, // Front face (Green)
        4, 4, 4, 4, 4, 4, 4, 4, 4, // Down face (Yellow)
        5, 5, 5, 5, 5, 5, 5, 5, 5, // Left face (Orange)
        6, 6, 6, 6, 6, 6, 6, 6, 6   // Back face (Blue)
    };
    public static final int[][] CYCLES = {
        {  9, 10, 11,  18, 19, 20,  36, 37, 38,  45, 46, 47 },
        {  45, 48, 51,  35, 32, 29,  26, 23, 20,  8, 5, 2 },
        {  6,  7,  8,  9,  12, 15,  29, 28, 27,  44, 41, 38 },
        { 42, 43, 44,  24, 25, 26,  15, 16, 17,  51, 52, 53 },
        {  0,  3,  6,  18, 21, 24,  27, 30, 33,  53, 50, 47 },
        {  2,  1,  0,  36,  39, 42,  33, 34, 35,  17, 14, 11 },
    };
    public static final String[] NOTATIONS = {
        "U", "R", "F", "D", "L", "B",
        "U'", "R'", "F'", "D'", "L'", "B'",
        "U2", "R2", "F2", "D2", "L2", "B2"
    };
    private static final char[] COLOR_TO_FACELET = {'U', 'R', 'F', 'D', 'L', 'B'};
    private Cube cube3D;
    private ArrayList<String> moveHistory = new ArrayList<>();
    private final Deque<int[]> undoStack = new ArrayDeque<>();
    private final Deque<int[]> redoStack = new ArrayDeque<>();

    public RubiksCube() { // Create an unscrambled Rubiks cube
        this(SOLVED_STATE.clone(), true);
    }

    public RubiksCube(int[] state) { // Create a Rubiks cube with a predetermined scramble
        this(state, true);
    }

    public RubiksCube(boolean with3D) {
        this(SOLVED_STATE.clone(), with3D);
    }

    public RubiksCube(int[] state, boolean with3D) {
        if (state.length != 54) {
            throw new IllegalArgumentException("State must have 54 elements.");
        }
        this.state = state.clone();
        if (with3D) {
            this.cube3D = new Cube();
            this.cube3D.buildCube(this.state);
        }
    }

    public static int[] solvedState() {
        return SOLVED_STATE.clone();
    }

    public Cube getCube3D() {
        return this.cube3D;
    }

    public int[] getState() {
        return state.clone();
    }

    public boolean isSolved() {
        return isSolved(state);
    }

    public static boolean isSolved(int[] state) {
        return Arrays.equals(state, SOLVED_STATE);
    }

    public String toFaceletString() {
        return toFaceletString(state);
    }

    public static String toFaceletString(int[] state) {
        if (state.length != 54) {
            throw new IllegalArgumentException("State must have 54 elements.");
        }
        char[] facelets = new char[54];
        for (int i = 0; i < 54; i++) {
            int color = state[i];
            if (color < 1 || color > 6) {
                throw new IllegalArgumentException("Invalid color at index " + i + ": " + color);
            }
            facelets[i] = COLOR_TO_FACELET[color - 1];
        }
        return new String(facelets);
    }

    public static int[] applyMove(int[] state, String notation) {
        int[] result = state.clone();
        rotate(result, notation);
        return result;
    }

    public static int[] applyMoves(int[] state, String moves) {
        int[] result = state.clone();
        if (moves == null || moves.isBlank()) {
            return result;
        }
        for (String move : moves.trim().split("\\s+")) {
            rotate(result, move);
        }
        return result;
    }

    public void applyMoves(String moves) {
        if (moves == null || moves.isBlank()) {
            return;
        }
        recordSnapshot();
        for (String move : moves.trim().split("\\s+")) {
            rotate(state, move);
            appendToMoveHistory(move);
        }
        rebuild3D();
        checkSolvedMessage();
    }

    public void setState(int[] newState) {
        if (newState.length != 54) {
            throw new IllegalArgumentException("State must have 54 elements.");
        }
        recordSnapshot();
        this.state = newState.clone();
        moveHistory.clear();
        rebuild3D();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        redoStack.push(state.clone());
        state = undoStack.pop();
        moveHistory.clear();
        rebuild3D();
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        undoStack.push(state.clone());
        state = redoStack.pop();
        moveHistory.clear();
        rebuild3D();
    }

    private void recordSnapshot() {
        undoStack.push(state.clone());
        redoStack.clear();
    }

    private void rebuild3D() {
        if (cube3D != null) {
            cube3D.buildCube(state);
        }
    }

    private void appendToMoveHistory(String notation) {
        int moveIndex = Arrays.asList(NOTATIONS).indexOf(notation);
        if (moveIndex >= 12) {
            moveHistory.add(notation);
        } else {
            int face = moveIndex % 6;
            int direction = (moveIndex < 6) ? 1 : -1;
            int historyIndex = face + (direction == 1 ? 0 : 6);
            moveHistory.add(NOTATIONS[historyIndex]);
        }
    }

    private void checkSolvedMessage() {
        if (isSolved()) {
            System.out.println("Cube solved:");
            System.out.println("Move history: " + getMoveHistory());
            System.out.println("Total moves: " + moveHistory.size());
            printCube();
        }
    }

    public List<String> solve() {
        return new TwoPhaseSolver().solveMoves(getState());
    }

    public void printState() {
        System.out.println("Current cube state:");
        System.out.println(Arrays.toString(state));
    }

    public String getMoveHistory() {
        return String.join(" ", moveHistory);
    }

    public void printMoveHistory() {
        System.out.println("Current move history:");
        System.out.println(moveHistory);
    }

    // rotation logic

    public void rotate(String notation) {
        recordSnapshot();
        rotate(this.state, notation);
        appendToMoveHistory(notation);
        rebuild3D();
        checkSolvedMessage();
    }

    public void rotate(int face, int direction) {
        recordSnapshot();
        rotate(this.state, face, direction);

        int moveIndex = face + (direction == 1 ? 0 : 6);
        moveHistory.add(NOTATIONS[moveIndex]);

        rebuild3D();
        checkSolvedMessage();
    }

    public static void rotate(int[] state, String notation) {
        int moveIndex = Arrays.asList(NOTATIONS).indexOf(notation);

        if (moveIndex == -1) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }

        int face = moveIndex % 6;
        
        if (moveIndex >= 12) {
            rotate(state, face, 1);
            rotate(state, face, 1);
        } else {
            int direction = (moveIndex < 6) ? 1 : -1;
            rotate(state, face, direction);
        }
    }

    public static void rotate(int[] state, int face, int direction) {
        rotateFace(state, face, direction);
        rotateSides(state, face, direction);
    }

    // rotate face only

    private static void rotateFace(int[] state, int face, int direction) {
        int start = face * 9;
        int[] tempState = state.clone();

        if (direction == 1) { // Clockwise
            state[start + 0] = tempState[start + 6];
            state[start + 1] = tempState[start + 3];
            state[start + 2] = tempState[start + 0];
            state[start + 3] = tempState[start + 7];
            state[start + 5] = tempState[start + 1];
            state[start + 6] = tempState[start + 8];
            state[start + 7] = tempState[start + 5];
            state[start + 8] = tempState[start + 2];
        } else if (direction == -1) { // Counter-clockwise
            state[start + 0] = tempState[start + 2];
            state[start + 1] = tempState[start + 5];
            state[start + 2] = tempState[start + 8];
            state[start + 3] = tempState[start + 1];
            state[start + 5] = tempState[start + 7];
            state[start + 6] = tempState[start + 0];
            state[start + 7] = tempState[start + 3];
            state[start + 8] = tempState[start + 6];
        } else {
            throw new IllegalArgumentException("Direction must be 1 (clockwise) or -1 (counter-clockwise).");
        }
    }

    // rotate tiles adjacent to rotation

    private static void rotateSides(int[] state, int face, int direction) {
        int[] tempState = state.clone();
        int[] cycle = CYCLES[face];

        if (direction == 1) { // Clockwise: group i gets group i+3 (mod 12)
            for (int i = 0; i < 12; i++) {
                state[cycle[i]] = tempState[cycle[(i + 9) % 12]];
            }
        } else { // Counter-clockwise: group i gets group i+3 (mod 12)
            for (int i = 0; i < 12; i++) {
                state[cycle[i]] = tempState[cycle[(i + 3) % 12]];
            }
        }
    }

    // does exactly what it says lmaoo

    public void printCube() {
        System.out.println("Current Cube State:");
        System.out.println(Arrays.toString(state));

        java.util.function.BiFunction<String,Integer,String> center = (s,w) -> {
            if (s == null) return "";
            if (s.length() >= w) return s;
            int left = (w - s.length()) / 2;
            int right = w - s.length() - left;
            return " ".repeat(left) + s + " ".repeat(right);
        };

        int tileWidth = 6;
        int faceWidth = tileWidth * 3;
        String indent = " ".repeat(18);

        System.out.print(indent);
        System.out.println(center.apply("Up", faceWidth));

        for (int row = 0; row < 3; row++) {
            System.out.print("                  ");
            for (int col = 0; col < 3; col++) {
                int idx = 0 * 9 + row * 3 + col;
                System.out.print(String.format("%d(%02d) ", state[idx], idx));
            }
            System.out.println();
        }

        String[] midLabels = new String[]{"Left", "Front", "Right", "Back"};
        StringBuilder midLabelLine = new StringBuilder();
        for (String l : midLabels) midLabelLine.append(center.apply(l, faceWidth));
        System.out.println(midLabelLine.toString());

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int idx = 4 * 9 + row * 3 + col;
                System.out.print(String.format("%d(%02d) ", state[idx], idx));
            }
            for (int col = 0; col < 3; col++) {
                int idx = 2 * 9 + row * 3 + col;
                System.out.print(String.format("%d(%02d) ", state[idx], idx));
            }
            for (int col = 0; col < 3; col++) {
                int idx = 1 * 9 + row * 3 + col;
                System.out.print(String.format("%d(%02d) ", state[idx], idx));
            }
            for (int col = 0; col < 3; col++) {
                int idx = 5 * 9 + row * 3 + col;
                System.out.print(String.format("%d(%02d) ", state[idx], idx));
            }
            System.out.println();
        }

        System.out.print(indent);
        System.out.println(center.apply("Down", faceWidth));

        for (int row = 0; row < 3; row++) {
            System.out.print("                  ");
            for (int col = 0; col < 3; col++) {
                int idx = 3 * 9 + row * 3 + col;
                System.out.print(String.format("%d(%02d) ", state[idx], idx));
            }
            System.out.println();
        }
    }
}
