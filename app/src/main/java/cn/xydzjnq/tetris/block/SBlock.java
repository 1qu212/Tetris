package cn.xydzjnq.tetris.block;

import java.util.Arrays;

public class SBlock extends Block {
    //    private int[][] blockShapes = new int[][]{
//            {
//                    0, 0, 0, 0,
//                    0, 0, 1, 1,
//                    0, 1, 1, 0,
//                    0, 0, 0, 0
//            },
//            {
//                    0, 0, 0, 0,
//                    0, 1, 0, 0,
//                    0, 1, 1, 0,
//                    0, 0, 1, 0
//            },
//            {
//                    0, 0, 0, 0,
//                    0, 1, 1, 0,
//                    1, 1, 0, 0,
//                    0, 0, 0, 0
//            },
//            {
//                    0, 1, 0, 0,
//                    0, 1, 1, 0,
//                    0, 0, 1, 0,
//                    0, 0, 0, 0
//            }
//    };
    private int[][] blockShapes = new int[][]{
            {
                    0, 1, 1, 0,
                    1, 1, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0
            },
            {
                    1, 0, 0, 0,
                    1, 1, 0, 0,
                    0, 1, 0, 0,
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
        initalRow = 3;
        return Arrays.copyOf(blockShapes[state], 8);
    }

//    @Override
//    public int[] nextShape() {
//        switch (state) {
//            case 0:
//                state = 1;
//                break;
//            case 1:
//                state = 2;
//                break;
//            case 2:
//                state = 3;
//                break;
//            case 3:
//                state = 0;
//                break;
//        }
//        return getShape();
//    }

    @Override
    public int[] nextShape() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 0;
                break;
        }
        return getShape();
    }

    @Override
    public int[] previousShape() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 0;
                break;
        }
        return getShape();
    }

    private void initState() {
        state = 0;
    }
}
