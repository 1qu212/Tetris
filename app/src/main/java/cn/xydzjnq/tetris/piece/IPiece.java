package cn.xydzjnq.tetris.piece;

import java.util.Arrays;

import static cn.xydzjnq.tetris.MainActivity.BOARDCULUMN;

public class IPiece extends Piece {
    private int[][] pieceArrays = new int[][]{
            {
                    0, 1, 0, 0,
                    0, 1, 0, 0,
                    0, 1, 0, 0,
                    0, 1, 0, 0
            },
            {
                    0, 0, 0, 0,
                    1, 1, 1, 1,
                    0, 0, 0, 0,
                    0, 0, 0, 0
            }
    };

    @Override
    public int[] getPieceArray() {
        return pieceArrays[state];
    }

    @Override
    public int[] getSimplePieceArray() {
        initState();
        initalRow = 3;
        return Arrays.copyOf(pieceArrays[state], 8);
    }

    @Override
    public int[] nextStatePieceArray() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 0;
                break;
        }
        return getPieceArray();
    }

    @Override
    public int[] previousStatePieceArray() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 0;
                break;
        }
        return getPieceArray();
    }

    @Override
    public boolean isCollision(int culumn) {
        switch (state) {
            default:
                return true;
            case 0:
                if (culumn >= 0 && culumn <= BOARDCULUMN - 1) {
                    return false;
                } else {
                    return true;
                }
            case 1:
                if (culumn >= 1 && culumn <= BOARDCULUMN - 3) {
                    return false;
                } else {
                    return true;
                }
        }
    }

    private void initState() {
        state = 1;
    }
}
