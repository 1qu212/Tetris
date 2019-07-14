package cn.xydzjnq.tetris.bean;

public class StateBean {
    //方块片左下角在整个界面的行和列
    private int row;
    private int culumn;
    //界面中的方块片数组
    private int[] currentPieceArray;
    private String currentShape;
    private int currentState;
    //“下一个”方块片数组
    private int[] nextPieceArray;
    private String nextShape;
    private int nextState;
    //已经确定的（不含空中方块片）的界面数组
    private int[] blockBoardArray;
    //用于更新界面的数组（可含空中方块片，也可不含空中方块片）
    private int[] tempBlockBoardArray;
    //等级
    private int level = 1;
    //分数
    private int score = 0;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCulumn() {
        return culumn;
    }

    public void setCulumn(int culumn) {
        this.culumn = culumn;
    }

    public int[] getCurrentPieceArray() {
        return currentPieceArray;
    }

    public void setCurrentPieceArray(int[] currentPieceArray) {
        this.currentPieceArray = currentPieceArray;
    }

    public int[] getNextPieceArray() {
        return nextPieceArray;
    }

    public void setNextPieceArray(int[] nextPieceArray) {
        this.nextPieceArray = nextPieceArray;
    }

    public int[] getBlockBoardArray() {
        return blockBoardArray;
    }

    public void setBlockBoardArray(int[] blockBoardArray) {
        this.blockBoardArray = blockBoardArray;
    }

    public int[] getTempBlockBoardArray() {
        return tempBlockBoardArray;
    }

    public void setTempBlockBoardArray(int[] tempBlockBoardArray) {
        this.tempBlockBoardArray = tempBlockBoardArray;
    }

    public String getCurrentShape() {
        return currentShape;
    }

    public void setCurrentShape(String currentShape) {
        this.currentShape = currentShape;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public String getNextShape() {
        return nextShape;
    }

    public void setNextShape(String nextShape) {
        this.nextShape = nextShape;
    }

    public int getNextState() {
        return nextState;
    }

    public void setNextState(int nextState) {
        this.nextState = nextState;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
