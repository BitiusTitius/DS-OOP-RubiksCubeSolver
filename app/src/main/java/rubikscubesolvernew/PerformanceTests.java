package rubikscubesolvernew;

import java.util.*;

public class PerformanceTests {
    // copied from rubikscube.java
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

    private static final int MOVE_COUNT = 18;
    private static final int[] INPUT_SIZES = {1_000, 10_000, 100_000, 1_000_000};
    private static final int TRIALS = 7;
    private static final int WARMUP_REPS = 3;

    public static void main(String[] args) {
        System.out.println("=== Rubik's Cube – Data Structure Benchmark ===\n");

        int maxMoves = INPUT_SIZES[INPUT_SIZES.length - 1];
        int[] moveSequence = generateMoves(maxMoves, 42L);

        warmup(moveSequence);

        String header = String.format("%-28s %-14s %12s", "Data Structure", "Input Size", "Time (ms)");
        String divider = "-".repeat(header.length());
        System.out.println(header);
        System.out.println(divider);

        String[] names = { "int[]", "int[][]", "ArrayList<Integer>", "HashMap<Integer,Integer>" };

        for (int inputSize : INPUT_SIZES) {
            int[] moves = Arrays.copyOf(moveSequence, inputSize);

            long[] times = new long[TRIALS];

            // 1d array
            for (int t = 0; t < TRIALS; t++) times[t] = benchmarkIntArray(moves);
            System.out.printf("%-28s %-14d %12.3f%n", names[0], inputSize, median(times) / 1e6);

            // 2d array
            for (int t = 0; t < TRIALS; t++) times[t] = benchmarkInt2D(moves);
            System.out.printf("%-28s %-14d %12.3f%n", names[1], inputSize, median(times) / 1e6);

            // arraylist
            for (int t = 0; t < TRIALS; t++) times[t] = benchmarkArrayList(moves);
            System.out.printf("%-28s %-14d %12.3f%n", names[2], inputSize, median(times) / 1e6);

            // hashmap
            for (int t = 0; t < TRIALS; t++) times[t] = benchmarkHashMap(moves);
            System.out.printf("%-28s %-14d %12.3f%n", names[3], inputSize, median(times) / 1e6);

            System.out.println(divider);
        }

        System.out.println("\nAll times are the median of " + TRIALS + " runs (milliseconds).");
    }

    // benchmark wrappers

    private static long benchmarkIntArray(int[] moves) {
        int[] state = SOLVED_STATE.clone();
        long start = System.nanoTime();
        for (int move : moves) rotateIntArray(state, move);
        return System.nanoTime() - start;
    }

    private static long benchmarkInt2D(int[] moves) {
        int[][] state = to2D(SOLVED_STATE);
        long start = System.nanoTime();
        for (int move : moves) applyMove_Int2D(state, move);
        return System.nanoTime() - start;
    }

    private static long benchmarkArrayList(int[] moves) {
        ArrayList<Integer> state = toArrayList(SOLVED_STATE);
        long start = System.nanoTime();
        for (int move : moves) applyMove_ArrayList(state, move);
        return System.nanoTime() - start;
    }

    private static long benchmarkHashMap(int[] moves) {
        HashMap<Integer, Integer> state = toHashMap(SOLVED_STATE);
        long start = System.nanoTime();
        for (int move : moves) applyMove_HashMap(state, move);
        return System.nanoTime() - start;
    }

    // rotation logic for intarray

    private static void rotateIntArray(int[] state, int moveIndex) {
        int face      = moveIndex % 6;
        int direction = (moveIndex < 6) ? 1 : (moveIndex < 12) ? -1 : 0; // 0 = double

        if (direction == 0) {
            rotateFace_IntArray(state, face, 1);
            rotateSides_IntArray(state, face, 1);
            rotateFace_IntArray(state, face, 1);
            rotateSides_IntArray(state, face, 1);
        } else {
            rotateFace_IntArray(state, face, direction);
            rotateSides_IntArray(state, face, direction);
        }
    }

    private static void rotateFace_IntArray(int[] state, int face, int direction) { // copied from rubikscube.java
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
        }
    }

    private static void rotateSides_IntArray(int[] state, int face, int direction) { // copied from rubikscube.java
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

    // rotation logic for 2d int

    private static int[][] to2D(int[] state) { // convert 1d array to 2d
        int[][] d = new int[6][9]; // 6 faces, 9 tiles each
        for (int i = 0; i < 54; i++) d[i / 9][i % 9] = state[i];
        return d;
    }

    private static int get2D(int[][] s, int idx)              { return s[idx/9][idx%9]; }
    private static void set2D(int[][] s, int idx, int val)    { s[idx/9][idx%9] = val;  }

    private static void applyMove_Int2D(int[][] state, int moveIndex) {
        int face      = moveIndex % 6;
        int direction = (moveIndex < 6) ? 1 : (moveIndex < 12) ? -1 : 0;

        if (direction == 0) {
            rotateFace_Int2D(state, face, 1);
            rotateSides_Int2D(state, face, 1);
            rotateFace_Int2D(state, face, 1);
            rotateSides_Int2D(state, face, 1);
        } else {
            rotateFace_Int2D(state, face, direction);
            rotateSides_Int2D(state, face, direction);
        }
    }

    private static void rotateFace_Int2D(int[][] state, int face, int direction) {
        int[] tempState = state[face].clone();

        if (direction == 1) { // Clockwise
            state[face][0] = tempState[6];
            state[face][1] = tempState[3];
            state[face][2] = tempState[0];
            state[face][3] = tempState[7];
            state[face][5] = tempState[1];
            state[face][6] = tempState[8];
            state[face][7] = tempState[5];
            state[face][8] = tempState[2];
        } else if (direction == -1) { // Counter-clockwise
            state[face][0] = tempState[2];
            state[face][1] = tempState[5];
            state[face][2] = tempState[8];
            state[face][3] = tempState[1];
            state[face][5] = tempState[7];
            state[face][6] = tempState[0];
            state[face][7] = tempState[3];
            state[face][8] = tempState[6];
        }
    }

    private static void rotateSides_Int2D(int[][] state, int face, int direction) {
        int[] cycle = CYCLES[face];
        int[] tempState  = new int[12];
        for (int i = 0; i < 12; i++) tempState[i] = get2D(state, cycle[i]);

        if (direction == 1) {
            for (int i = 0; i < 12; i++) {
                set2D(state, cycle[i], tempState[(i + 9) % 12]);
            }
        } else {
            for (int i = 0; i < 12; i++) {
                set2D(state, cycle[i], tempState[(i + 3) % 12]);
            }
        }
    }

    // rotation logic for arraylist

    private static ArrayList<Integer> toArrayList(int[] state) { // convert 1d array to arraylist
        ArrayList<Integer> list = new ArrayList<>(54);
        for (int tile : state) list.add(tile);
        return list;
    }

    private static void applyMove_ArrayList(ArrayList<Integer> state, int moveIndex) {
        int face      = moveIndex % 6;
        int direction = (moveIndex < 6) ? 1 : (moveIndex < 12) ? -1 : 0;

        if (direction == 0) {
            rotateFace_ArrayList(state, face, 1);
            rotateSides_ArrayList(state, face, 1);
            rotateFace_ArrayList(state, face, 1);
            rotateSides_ArrayList(state, face, 1);
        } else {
            rotateFace_ArrayList(state, face, direction);
            rotateSides_ArrayList(state, face, direction);
        }
    }

    private static void rotateFace_ArrayList(ArrayList<Integer> state, int face, int direction) {
        int start = face * 9;
        int[] tempState = new int[9];
        for (int i = 0; i < 9; i++) tempState[i] = state.get(start + i);

        if (direction == 1) {
            state.set(start + 0, tempState[6]);
            state.set(start + 1, tempState[3]);
            state.set(start + 2, tempState[0]);
            state.set(start + 3, tempState[7]);
            state.set(start + 5, tempState[1]);
            state.set(start + 6, tempState[8]);
            state.set(start + 7, tempState[5]);
            state.set(start + 8, tempState[2]);
        } else if (direction == -1) {
            state.set(start + 0, tempState[2]);
            state.set(start + 1, tempState[5]);
            state.set(start + 2, tempState[8]);
            state.set(start + 3, tempState[1]);
            state.set(start + 5, tempState[7]);
            state.set(start + 6, tempState[0]);
            state.set(start + 7, tempState[3]);
            state.set(start + 8, tempState[6]);
        }
    }

    private static void rotateSides_ArrayList(ArrayList<Integer> state, int face, int direction) {
        int[] cycle = CYCLES[face];
        int[] tempState  = new int[12];
        for (int i = 0; i < 12; i++) tempState[i] = state.get(cycle[i]);

        if (direction == 1) {
            for (int i = 0; i < 12; i++) {
                state.set(cycle[i], tempState[(i + 9) % 12]);
            }
        } else {
            for (int i = 0; i < 12; i++) {
                state.set(cycle[i], tempState[(i + 3) % 12]);
            }
        }
    }

    // rotation logic for hashmap

    private static HashMap<Integer, Integer> toHashMap(int[] state) { // convert 1d array to hashmap
        HashMap<Integer, Integer> map = new HashMap<>(128);
        for (int i = 0; i < state.length; i++) map.put(i, state[i]);
        return map;
    }

    private static void applyMove_HashMap(HashMap<Integer, Integer> state, int moveIndex) {
        int face      = moveIndex % 6;
        int direction = (moveIndex < 6) ? 1 : (moveIndex < 12) ? -1 : 0;

        if (direction == 0) {
            rotateFace_HashMap(state, face, 1);
            rotateSides_HashMap(state, face, 1);
            rotateFace_HashMap(state, face, 1);
            rotateSides_HashMap(state, face, 1);
        } else {
            rotateFace_HashMap(state, face, direction);
            rotateSides_HashMap(state, face, direction);
        }
    }

    private static void rotateFace_HashMap(HashMap<Integer, Integer> state, int face, int direction) {
        int start = face * 9;
        int[] tempState = new int[9];
        for (int i = 0; i < 9; i++) tempState[i] = state.get(start + 1);

        if (direction == 1) {
            state.put(start + 0, tempState[6]);
            state.put(start + 1, tempState[3]);
            state.put(start + 2, tempState[0]);
            state.put(start + 3, tempState[7]);
            state.put(start + 5, tempState[1]);
            state.put(start + 6, tempState[8]);
            state.put(start + 7, tempState[5]);
            state.put(start + 8, tempState[2]);
        } else if (direction == -1) {
            state.put(start + 0, tempState[2]);
            state.put(start + 1, tempState[5]);
            state.put(start + 2, tempState[8]);
            state.put(start + 3, tempState[1]);
            state.put(start + 5, tempState[7]);
            state.put(start + 6, tempState[0]);
            state.put(start + 7, tempState[3]);
            state.put(start + 8, tempState[6]);
        }
    }

    private static void rotateSides_HashMap(HashMap<Integer, Integer> state, int face, int direction) {
        int[] cycle = CYCLES[face];
        int[] tempState = new int[12];

        if (direction == 1) {
            for (int i = 0; i < 12; i++) {
                state.put(cycle[i], tempState[(i + 9) % 12]);
            }
        } else {
            for (int i = 0; i < 12; i++) {
                state.put(cycle[i], tempState[(i + 3) % 12]);
            }
        }
    }

    // other functions

    private static int[] generateMoves(int count, long seed) { // generate random move sequence
        Random rng = new Random(seed);
        int[] moves = new int[count];
        for (int i = 0; i < count; i++) moves[i] = rng.nextInt(MOVE_COUNT);
        return moves;
    }

    private static long median(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    private static void warmup(int[] moveSequence) {
        System.out.println("Warming up JIT (" + WARMUP_REPS + " passes)...");
        int[] moves = Arrays.copyOf(moveSequence, 100_000);
        for (int r = 0; r < WARMUP_REPS; r++) {
            int[] s1 = SOLVED_STATE.clone();
            for (int m : moves) rotateIntArray(s1, m);

            int[][] s2 = to2D(SOLVED_STATE);
            for (int m : moves) applyMove_Int2D(s2, m);

            ArrayList<Integer> s3 = toArrayList(SOLVED_STATE);
            for (int m : moves) applyMove_ArrayList(s3, m);

            HashMap<Integer, Integer> s4 = toHashMap(SOLVED_STATE);
            for (int m : moves) applyMove_HashMap(s4, m);
        }
        System.out.println("Warm-up complete.\n");
    }
}