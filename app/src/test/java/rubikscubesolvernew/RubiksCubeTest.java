package rubikscubesolvernew;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RubiksCubeTest {
    private static final String SOLVED_FACELETS =
            "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB";

    @Test
    void solvedCubeIsSolved() {
        RubiksCube cube = new RubiksCube(false);
        assertTrue(cube.isSolved());
    }

    @Test
    void singleMoveIsNotSolved() {
        int[] state = RubiksCube.applyMove(RubiksCube.solvedState(), "R");
        assertFalse(RubiksCube.isSolved(state));
    }

    @Test
    void fourQuarterTurnsReturnToSolved() {
        int[] state = RubiksCube.solvedState();
        state = RubiksCube.applyMove(state, "R");
        state = RubiksCube.applyMove(state, "R");
        state = RubiksCube.applyMove(state, "R");
        state = RubiksCube.applyMove(state, "R");
        assertTrue(RubiksCube.isSolved(state));
    }

    @Test
    void moveAndInverseReturnToSolved() {
        int[] state = RubiksCube.applyMoves(RubiksCube.solvedState(), "R R'");
        assertTrue(RubiksCube.isSolved(state));
    }

    @Test
    void doubleMoveEqualsTwoSingleMoves() {
        int[] fromDouble = RubiksCube.applyMove(RubiksCube.solvedState(), "U2");
        int[] fromSingles = RubiksCube.applyMoves(RubiksCube.solvedState(), "U U");
        assertArrayEquals(fromDouble, fromSingles);
    }

    @Test
    void toFaceletStringOnSolvedCube() {
        RubiksCube cube = new RubiksCube(false);
        assertEquals(SOLVED_FACELETS, cube.toFaceletString());
        assertEquals(SOLVED_FACELETS, RubiksCube.toFaceletString(cube.getState()));
    }

    @Test
    void staticApplyMatchesInstanceRotate() {
        RubiksCube cube = new RubiksCube(false);
        cube.rotate("R");
        cube.rotate("U");
        cube.rotate("F'");

        int[] expected = RubiksCube.applyMoves(RubiksCube.solvedState(), "R U F'");
        assertArrayEquals(expected, cube.getState());
    }

    @Test
    void applyMovesOnEmptyStringIsIdentity() {
        int[] solved = RubiksCube.solvedState();
        assertArrayEquals(solved, RubiksCube.applyMoves(solved, ""));
        assertArrayEquals(solved, RubiksCube.applyMoves(solved, "   "));
    }

    @Test
    void invalidNotationThrows() {
        int[] state = RubiksCube.solvedState();
        assertThrows(IllegalArgumentException.class, () -> RubiksCube.applyMove(state, "X"));
    }

    @Test
    void invalidColorInFaceletStringThrows() {
        int[] state = RubiksCube.solvedState();
        state[0] = 9;
        assertThrows(IllegalArgumentException.class, () -> RubiksCube.toFaceletString(state));
    }

    @Test
    void undoRestoresPreviousState() {
        RubiksCube cube = new RubiksCube(false);
        int[] solved = cube.getState();
        cube.rotate("R");
        assertFalse(cube.isSolved());
        assertTrue(cube.canUndo());
        cube.undo();
        assertArrayEquals(solved, cube.getState());
        assertFalse(cube.canUndo());
        assertTrue(cube.canRedo());
    }

    @Test
    void redoReappliesUndoneState() {
        RubiksCube cube = new RubiksCube(false);
        cube.rotate("F");
        int[] afterMove = cube.getState();
        cube.undo();
        cube.redo();
        assertArrayEquals(afterMove, cube.getState());
    }

    @Test
    void setStateCanBeUndone() {
        RubiksCube cube = new RubiksCube(false);
        int[] solved = cube.getState();
        int[] scrambled = RubiksCube.applyMove(solved, "U");
        cube.setState(scrambled);
        assertArrayEquals(scrambled, cube.getState());
        cube.undo();
        assertArrayEquals(solved, cube.getState());
    }

    @Test
    void applyMovesRecordsSingleUndoStep() {
        RubiksCube cube = new RubiksCube(false);
        int[] solved = cube.getState();
        cube.applyMoves("R U R' U'");
        assertFalse(cube.isSolved());
        cube.undo();
        assertArrayEquals(solved, cube.getState());
    }

    @Test
    void newActionClearsRedoStack() {
        RubiksCube cube = new RubiksCube(false);
        cube.rotate("R");
        cube.undo();
        assertTrue(cube.canRedo());
        cube.rotate("U");
        assertFalse(cube.canRedo());
    }
}
