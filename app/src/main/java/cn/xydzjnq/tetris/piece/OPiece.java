package cn.xydzjnq.tetris.piece;

import java.util.Arrays;

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
    public int[] getSimplePieceArray() {
        initalRow = 2;
        return Arrays.copyOfRange(pieceArrays[state], 4, 12);
    }
}
