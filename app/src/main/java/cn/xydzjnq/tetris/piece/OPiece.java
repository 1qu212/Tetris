package cn.xydzjnq.tetris.piece;

import java.util.Arrays;

import static cn.xydzjnq.tetris.MainActivity.BOARDCULUMN;

public class OPiece extends Piece {
    private int[][] pieceArrays = new int[][]{
            {
                    0, 0, 0, 0,
                    0, 1, 1, 0,
                    0, 1, 1, 0,
                    0, 0, 0, 0
            }
    };

    @Override
    public int[] getPieceArray() {
        return pieceArrays[state];
    }

    @Override
    public int[] nextStatePieceArray() {
        return getPieceArray();
    }

    @Override
    public int[] previousStatePieceArray() {
        return getPieceArray();
    }

    @Override
    public boolean isCollision(int culumn) {
        if (culumn >= 0 && culumn <= BOARDCULUMN - 2) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int[] getSimplePieceArray() {
        initalRow = 2;
        return Arrays.copyOfRange(pieceArrays[state], 4, 12);
    }
}
