package cn.xydzjnq.tetris.block;

import java.util.Arrays;
import java.util.Random;

public class LBlock extends Block {
    //    private int[][] blockShapes = new int[][]{
//            {
//                    0, 1, 0, 0,
//                    0, 1, 0, 0,
//                    0, 1, 1, 0,
//                    0, 0, 0, 0
//            },
//            {
//                    0, 0, 0, 0,
//                    0, 1, 1, 1,
//                    0, 1, 0, 0,
//                    0, 0, 0, 0
//            },
//            {
//                    0, 0, 0, 0,
//                    0, 1, 1, 0,
//                    0, 0, 1, 0,
//                    0, 0, 1, 0
//            },
//            {
//                    0, 0, 0, 0,
//                    0, 0, 1, 0,
//                    1, 1, 1, 0,
//                    0, 0, 0, 0
//            }
//    };
    private int[][] blockShapes = new int[][]{
            {
                    1, 0, 0, 0,
                    1, 0, 0, 0,
                    1, 1, 0, 0,
                    0, 0, 0, 0
            },
            {
                    1, 1, 1, 0,
                    1, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0
            },
            {
                    0, 1, 1, 0,
                    0, 0, 1, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 0
            },
            {
                    0, 0, 0, 0,
                    0, 0, 1, 0,
                    1, 1, 1, 0,
                    0, 0, 0, 0
            }
    };

    @Override
    public int[] getShape() {
        return blockShapes[state];
    }

    @Override
    public int[] getSimpleShape() {
        initState();
        int[] simpleBlockShape = new int[8];
        switch (state) {
            case 1:
                initalRow = 3;
                simpleBlockShape = Arrays.copyOf(blockShapes[state], 8);
                break;
            case 3:
                initalRow = 2;
                simpleBlockShape = Arrays.copyOfRange(blockShapes[state], 4, 12);
                break;
        }
        return simpleBlockShape;
    }

    @Override
    public int[] nextShape() {
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
        return getShape();
    }

    @Override
    public int[] previousShape() {
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
        return getShape();
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
