package cn.xydzjnq.tetris.block;

public abstract class Block {
    int state = 0;
    //在整个界面上初始行初始列（左下角）
    int initalRow = 1;
    int initalCulumn = 4;

    public abstract int[] getShape();

    public abstract int[] getSimpleShape();

    public abstract int[] nextShape();

    public abstract int[] previousShape();

    public int getInitalRow() {
        return initalRow;
    }

    public int getInitalCulumn() {
        return initalCulumn;
    }
}
