package cn.xydzjnq.tetris.block;

import java.util.Arrays;

public class OBlock extends Block {
    private int[][] blockShapes = new int[][]{
            {
                    0, 0, 0, 0,
                    0, 1, 1, 0,
                    0, 1, 1, 0,
                    0, 0, 0, 0
            }
    };

    @Override
    public int[] getShape() {
        return blockShapes[state];
    }

    @Override
    public int[] nextShape() {
        return getShape();
    }

    @Override
    public int[] previousShape() {
        return getShape();
    }

    @Override
    public int[] getSimpleShape() {
        initalRow = 2;
        return Arrays.copyOfRange(blockShapes[state], 4, 12);
    }
}
