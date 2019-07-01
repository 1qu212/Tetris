package cn.xydzjnq.tetris.piece;

public abstract class Piece {
    int state = 0;
    //在整个界面上初始行初始列（即方块片左下角在整个界面的位置）
    int initalRow = 1;
    int initalCulumn = 4;

    /**
     *
     * @return 代表一个方块片的数组
     */
    public abstract int[] getPieceArray();

    /**
     *
     * @return 即代表方块片简化后的数组
     */
    public abstract int[] getSimplePieceArray();

    /**
     *
     * @return 方块片下一个形态的数组
     */
    public abstract int[] nextStatePieceArray();

    /**
     *
     * @return 方块片前一个形态的数组
     */
    public abstract int[] previousStatePieceArray();

    public int getInitalRow() {
        return initalRow;
    }

    public int getInitalCulumn() {
        return initalCulumn;
    }
}
