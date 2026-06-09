package rubikscubesolvernew;

import cs.min2phase.Search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TwoPhaseSolver {
    private static final int MAX_LENGTH = 21;
    private static final int PROBE_MAX = 10_000_000;

    private final Search search = new Search();

    public String solve(int[] state) {
        String facelets = RubiksCube.toFaceletString(state);
        String solution = search.solution(facelets, MAX_LENGTH, PROBE_MAX, 0, 0);
        if (solution.startsWith("Error")) {
            throw new IllegalStateException(solution);
        }
        return solution.trim();
    }

    public List<String> solveMoves(int[] state) {
        String solution = solve(state);
        if (solution.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(solution.split("\\s+"));
    }

    public static boolean isReachable(int[] state) {
        try {
            String facelets = RubiksCube.toFaceletString(state);
            Search search = new Search();
            String solution = search.solution(facelets, MAX_LENGTH, PROBE_MAX, 0, 0);
            return !solution.startsWith("Error");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
