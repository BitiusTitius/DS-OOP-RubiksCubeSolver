package rubikscubesolvernew;

import cs.min2phase.Tools;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwoPhaseSolverTest {
    private final TwoPhaseSolver solver = new TwoPhaseSolver();

    @Test
    void solvedCubeReturnsEmptySolution() {
        int[] state = RubiksCube.solvedState();
        assertEquals("", solver.solve(state));
        assertTrue(solver.solveMoves(state).isEmpty());
    }

    @Test
    void singleMoveScrambleIsSolvedByInverse() {
        int[] state = RubiksCube.applyMove(RubiksCube.solvedState(), "R");
        String solution = solver.solve(state);
        int[] solved = RubiksCube.applyMoves(state, solution);
        assertTrue(RubiksCube.isSolved(solved));
        assertTrue(solution.length() <= 25);
    }

    @Test
    void multiMoveScrambleIsSolved() {
        int[] state = RubiksCube.applyMoves(RubiksCube.solvedState(), "R U R' U'");
        String solution = solver.solve(state);
        int[] solved = RubiksCube.applyMoves(state, solution);
        assertTrue(RubiksCube.isSolved(solved));
        assertTrue(solution.split("\\s+").length <= 25);
    }

    @Test
    void wcaScrambleRoundTrip() {
        String scramble = "R U R' F' R U R' U' R' F R2 U' R' U'";
        String facelets = Tools.fromScramble(scramble);
        int[] state = faceletStringToState(facelets);
        String solution = solver.solve(state);
        int[] solved = RubiksCube.applyMoves(state, solution);
        assertTrue(RubiksCube.isSolved(solved));
        assertTrue(solution.split("\\s+").length <= 25);
    }

    @Test
    void rubiksCubeSolveDelegatesToSolver() {
        int[] state = RubiksCube.applyMove(RubiksCube.solvedState(), "F");
        RubiksCube cube = new RubiksCube(state, false);
        List<String> moves = cube.solve();
        assertFalse(moves.isEmpty());
        cube.applyMoves(String.join(" ", moves));
        assertTrue(cube.isSolved());
    }

    private static int[] faceletStringToState(String facelets) {
        int[] state = new int[54];
        for (int i = 0; i < 54; i++) {
            state[i] = switch (facelets.charAt(i)) {
                case 'U' -> 1;
                case 'R' -> 2;
                case 'F' -> 3;
                case 'D' -> 4;
                case 'L' -> 5;
                case 'B' -> 6;
                default -> throw new IllegalArgumentException("Invalid facelet: " + facelets.charAt(i));
            };
        }
        return state;
    }
}
