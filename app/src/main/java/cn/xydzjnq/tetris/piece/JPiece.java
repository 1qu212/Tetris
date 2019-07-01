package cn.xydzjnq.tetris.piece;

import java.util.Arrays;
import java.util.Random;

public class JPiece extends Piece {
    private int[][] pieceArrays = new int[][]{
            {
                    0, 0, 1, 0,
                    0, 0, 1, 0,
                    0, 1, 1, 0,
                    0, 0, 0, 0
            },
            {
                    0, 0, 0, 0,
                    1, 0, 0, 0,
                    1, 1, 1, 0,
                    0, 0, 0, 0
            },
            {
                    1, 1, 0, 0,
                    1, 0, 0, 0,
                    1, 0, 0, 0,
                    0, 0, 0, 0
            },
            {
                    1, 1, 1, 0,
                    0, 0, 1, 0,
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
        int[] simpleBlockShape = new int[8];
        switch (state) {
            case 1:
                initalRow = 2;
                simpleBlockShape = Arrays.copyOfRange(pieceArrays[state], 4, 12);
                break;
            case 3:
                initalRow = 3;
                simpleBlockShape = Arrays.copyOf(pieceArrays[state], 8);
                break;
        }
        return simpleBlockShape;
    }

    @Override
    public int[] nextStatePieceArray() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 2;
                break;
            case 2:
                state = 3;
                break;
            case 3:
                state = 0;
                break;
        }
        return getPieceArray();
    }

    @Override
    public int[] previousStatePieceArray() {
        switch (state) {
            case 0:
                state = 3;
                break;
            case 1:
                state = 0;
                break;
            case 2:
                state = 1;
                break;
            case 3:
                state = 2;
                break;
        }
        return getPieceArray();
    }

    private void initState() {
        Random random = new Random();
        int randomInt = random.nextInt(2);
        switch (randomInt) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 3;
                break;
        }
    }
}
