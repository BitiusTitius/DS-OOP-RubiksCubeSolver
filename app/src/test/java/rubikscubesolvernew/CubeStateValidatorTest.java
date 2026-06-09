package rubikscubesolvernew;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CubeStateValidatorTest {

    @Test
    void solvedStateIsValid() {
        CubeStateValidator.ValidationResult result =
            CubeStateValidator.validate(RubiksCube.solvedState());
        assertTrue(result.isValid(), result.message());
    }

    @Test
    void wrongCenterPairingFails() {
        int[] state = RubiksCube.solvedState();
        int temp = state[4];
        state[4] = state[13];
        state[13] = temp;
        CubeStateValidator.ValidationResult result = CubeStateValidator.validate(state);
        assertFalse(result.isValid());
        assertTrue(result.message().toLowerCase().contains("center"));
    }

    @Test
    void invalidEdgeFails() {
        int[] state = RubiksCube.applyMove(RubiksCube.solvedState(), "R");
        int temp = state[23];
        state[23] = state[21];
        state[21] = temp;
        CubeStateValidator.ValidationResult result = CubeStateValidator.validate(state);
        assertFalse(result.isValid());
        assertTrue(result.message().toLowerCase().contains("edge"));
    }

    @Test
    void invalidCornerFails() {
        int[] state = RubiksCube.applyMove(RubiksCube.solvedState(), "R");
        int temp = state[8];
        state[8] = state[31];
        state[31] = temp;
        CubeStateValidator.ValidationResult result = CubeStateValidator.validate(state);
        assertFalse(result.isValid());
        assertTrue(
            result.message().toLowerCase().contains("corner")
                || result.message().toLowerCase().contains("center")
                || result.message().toLowerCase().contains("reachable")
        );
    }

    @Test
    void wrongColorCountFails() {
        int[] state = RubiksCube.solvedState();
        state[0] = 2;
        CubeStateValidator.ValidationResult result = CubeStateValidator.validate(state);
        assertFalse(result.isValid());
        assertTrue(result.message().contains("9 times"));
    }

    @Test
    void unreachableStateFails() {
        int[][] edges = {
            {0, 45}, {2, 9}, {6, 18}, {8, 36},
            {27, 44}, {29, 15}, {33, 24}, {35, 53},
            {23, 12}, {21, 41}, {48, 14}, {50, 39}
        };
        int[][] corners = {
            {8, 11, 20}, {6, 18, 36}, {0, 36, 47}, {2, 9, 45},
            {35, 15, 26}, {33, 44, 24}, {27, 42, 53}, {29, 17, 51}
        };
        int[] base = RubiksCube.applyMoves(
            RubiksCube.solvedState(),
            "R U R' F' R U R' U' R U R' F' R U R' U'"
        );
        int[][] bases = {RubiksCube.solvedState(), base};

        for (int[] start : bases) {
            for (int[] edge : edges) {
                int[] state = start.clone();
                int temp = state[edge[0]];
                state[edge[0]] = state[edge[1]];
                state[edge[1]] = temp;
                if (assertUnreachableInvalid(state)) {
                    return;
                }
            }
            for (int[] corner : corners) {
                int[] state = start.clone();
                int a = state[corner[0]];
                int b = state[corner[1]];
                int c = state[corner[2]];
                state[corner[0]] = b;
                state[corner[1]] = c;
                state[corner[2]] = a;
                if (assertUnreachableInvalid(state)) {
                    return;
                }
            }
        }

        fail("Could not construct an unreachable but structurally valid test state.");
    }

    private static boolean assertUnreachableInvalid(int[] state) {
        if (!CubeStateValidator.validateStructure(state).isValid()) {
            return false;
        }
        if (TwoPhaseSolver.isReachable(state)) {
            return false;
        }
        CubeStateValidator.ValidationResult result = CubeStateValidator.validate(state);
        assertFalse(result.isValid());
        assertTrue(result.message().toLowerCase().contains("reachable"));
        return true;
    }
}
