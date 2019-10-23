package cn.xydzjnq.tetris;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.xydzjnq.tetris.bean.RecordListBean;
import cn.xydzjnq.tetris.bean.StateBean;
import cn.xydzjnq.tetris.dialog.NewRecordDialog;
import cn.xydzjnq.tetris.piece.Piece;
import cn.xydzjnq.tetris.piece.PieceFatory;
import cn.xydzjnq.tetris.util.ConfigSPUtils;
import cn.xydzjnq.tetris.util.StateSPUtils;
import cn.xydzjnq.tetris.view.GameOverView;
import cn.xydzjnq.tetris.view.LedTextView;
import cn.xydzjnq.tetris.view.ShadowImageView;

import static cn.xydzjnq.tetris.util.ConfigSPUtils.RECORDLIST;
import static cn.xydzjnq.tetris.util.StateSPUtils.STATEBEAN;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static String TAG = "MainActivity";
    private GridView gvBlockBoard;
    private LedTextView tvScore;
    private LedTextView tvLevel;
    private LedTextView tvMaxScore;
    private GridView gvNextPiece;
    private ShadowImageView btnPause;
    private ShadowImageView btnRecordList;
    private ShadowImageView btnRestart;
    private ShadowImageView btnSpace;
    private ShadowImageView btnUp;
    private ShadowImageView btnLeft;
    private ShadowImageView btnRight;
    private ShadowImageView btnDown;
    private Piece currentPiece;
    private GameOverView govAnim;
    private LinearLayout llAnim;
    private TextView tvUserName;
    //方块片左下角在整个界面的行和列
    private int row;
    private int culumn;
    //界面的行数和列数
    private final static int BOARDROW = 18;
    public final static int BOARDCULUMN = 10;
    //方块片的行数和列数
    private final static int PIECEROW = 4;
    private final static int PIECECULUMN = 4;
    private Piece nextPiece;
    //界面中的方块片数组
    private int[] currentPieceArray;
    //“下一个”方块片数组
    private int[] nextPieceArray;
    //已经确定的（不含空中方块片）的界面数组
    private int[] blockBoardArray;
    //用于更新界面的数组（可含空中方块片，也可不含空中方块片）
    private int[] tempBlockBoardArray;
    private BlockAdapter nextPieceAdapter;
    private BlockAdapter blockBoardAdapter;
    //非正在重玩状态
    private boolean isStart = true;
    //方块片下落定时器
    private Timer downTimer;
    //方块片快速下落定时器
    private Timer spaceTimer;
    //下落的时间间隔
    private int timeInterval = 800;
    //等级
    private int level = 1;
    //分数
    private int score = 0;
    //同一次消除方块片，每一行所获得的分数
    private int scoreStep = 100;
    private HandlerThread handlerThread;
    private Handler handler;
    private final static int RESTART = 1;
    private final static int UP = 2;
    private final static int LEFT = 3;
    private final static int RIGHT = 4;
    private final static int DOWN = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case RESTART:
                        isStart = false;
                        cancelDownTimer();
                        cancelSpaceTimer();
                        for (int i = BOARDROW; i > 0; i--) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            for (int j = 1; j <= BOARDCULUMN; j++) {
                                tempBlockBoardArray[(i - 1) * BOARDCULUMN + j - 1] = 1;
                            }
                            uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                        }
                        for (int i = 1; i <= BOARDROW; i++) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            for (int j = 1; j <= BOARDCULUMN; j++) {
                                tempBlockBoardArray[(i - 1) * BOARDCULUMN + j - 1] = 0;
                            }
                            uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                        }
                        blockBoardArray = Arrays.copyOf(tempBlockBoardArray, BOARDROW * BOARDCULUMN);
                        isStart = true;
                        uiHandler.sendEmptyMessage(RESET_DATA);
                        break;
                    case UP:
                        currentPieceArray = currentPiece.nextStatePieceArray();
                        if (isCollision()) {
                            currentPieceArray = currentPiece.previousStatePieceArray();
                        } else {
                            setTempBlockBoardArray();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                        }
                        break;
                    case LEFT:
                        culumn--;
                        if (isCollision()) {
                            culumn++;
                        } else {
                            setTempBlockBoardArray();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                        }
                        break;
                    case RIGHT:
                        culumn++;
                        if (isCollision()) {
                            culumn--;
                        } else {
                            setTempBlockBoardArray();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                        }
                        break;
                    case DOWN:
                        row++;
                        if (isCollision()) {
                            row--;
                            touchBottom();
                        } else {
                            setTempBlockBoardArray();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                        }
                        break;
                }
            }

            /**
             *
             * @return 下落或者左移右移旋转中的方块片是否与界面已有方块冲突
             */
            private boolean isCollision() {
                tempBlockBoardArray = Arrays.copyOf(blockBoardArray, BOARDROW * BOARDCULUMN);
                int count = 0;
                for (int i = row; i > row - PIECEROW && i > 0; i--) {
                    for (int j = culumn; j < culumn + PIECECULUMN; j++) {
                        if (i <= BOARDROW && j >= 1 && j <= BOARDCULUMN) {
                            if (currentPieceArray[(PIECEROW - (row - i) - 1) * PIECECULUMN + (j - culumn)] != 0) {
                                if (tempBlockBoardArray[(i - 1) * BOARDCULUMN + j - 1] != 0) {
                                    if (row <= currentPiece.getInitalRow()) {
                                        uiHandler.sendEmptyMessage(GAME_OVER);
                                        //这里是为了防止游戏结束还在touchBottom，还在REFRESH_NEXT_PIECE
                                        return false;
                                    }
                                    return true;
                                }
                                count++;
                            }
                        }
                    }
                }
                if (count == PIECEROW) {
                    return false;
                }
                if (row <= PIECEROW) {
                    return currentPiece.isCollision(culumn);
                }
                return true;
            }

            private void setTempBlockBoardArray() {
                tempBlockBoardArray = Arrays.copyOf(blockBoardArray, BOARDROW * BOARDCULUMN);
                for (int i = row; i > row - PIECEROW && i > 0; i--) {
                    for (int j = culumn; j < culumn + PIECECULUMN; j++) {
                        if (i <= BOARDROW && j >= 1 && j <= BOARDCULUMN) {
                            if (tempBlockBoardArray[(i - 1) * BOARDCULUMN + j - 1] == 0) {
                                tempBlockBoardArray[(i - 1) * BOARDCULUMN + j - 1] = currentPieceArray[(PIECEROW - (row - i) - 1) * PIECECULUMN + (j - culumn)];
                            }
                        }
                    }
                }
            }

            /**
             * 方块片到界面底部了
             */
            private void touchBottom() {
                cancelSpaceTimer();
                cancelDownTimer();
                uiHandler.sendEmptyMessage(PAUSE);
                setTempBlockBoardArray();
                //触底先保存现在的界面状态
                blockBoardArray = Arrays.copyOf(tempBlockBoardArray, BOARDROW * BOARDCULUMN);
                //再消除满行
                lineDispear();
                //第一行有亮方块则游戏结束
                for (int i = 0; i < BOARDCULUMN; i++) {
                    if (blockBoardArray[i] != 0) {
                        uiHandler.sendEmptyMessage(GAME_OVER);
                        return;
                    }
                }
                row = currentPiece.getInitalRow() - 1;
                currentPiece = nextPiece;
                currentPieceArray = currentPiece.getPieceArray();
                culumn = currentPiece.getInitalCulumn();
                nextPiece = PieceFatory.createPiece();
                nextPieceArray = nextPiece.getSimplePieceArray();
                scoreStep = 100;
                uiHandler.sendEmptyMessage(REFRESH_NEXT_PIECE);
                uiHandler.sendEmptyMessage(RESUME);
                downTimer = new Timer();
                downTimer.schedule(getTimerTask(), timeInterval, timeInterval);
            }

            /**
             * 消除满行
             */
            private void lineDispear() {
                for (int i = BOARDROW; i >= 1; i--) {
                    int count = 0;
                    for (int j = 1; j <= BOARDCULUMN; j++) {
                        if (tempBlockBoardArray[(i - 1) * BOARDCULUMN + j - 1] == 1) {
                            count++;
                        }
                    }
                    if (count == BOARDCULUMN) {
                        int splashCount = 5;
                        for (int k = 0; k < splashCount; k++) {
                            for (int j = 1; j <= BOARDCULUMN; j++) {
                                tempBlockBoardArray[(i - 1) * BOARDCULUMN + j - 1] = (k / 2 == 0) ? 0 : 1;
                            }
                            uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        for (int x = i; x >= 2; x--) {
                            System.arraycopy(tempBlockBoardArray, (x - 2) * 10 + 1 - 1, tempBlockBoardArray, (x - 1) * 10 + 1 - 1, BOARDCULUMN);
                        }
                        for (int x = 0; x < BOARDCULUMN; x++) {
                            tempBlockBoardArray[x] = 0;
                        }
                        blockBoardArray = Arrays.copyOf(tempBlockBoardArray, BOARDROW * BOARDCULUMN);
                        uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
                        score += scoreStep;
                        scoreStep += 20;
                        uiHandler.sendEmptyMessage(REFRESH_SCORE);
                        lineDispear();
                        break;
                    }
                }
            }
        };
    }

    private final static int REFRESH_BLOCK_BOARD = 100;
    private final static int REFRESH_NEXT_PIECE = 101;
    private final static int PAUSE_RESUME = 102;
    private final static int PAUSE = 103;
    private final static int RESUME = 104;
    private final static int REFRESH_SCORE = 105;
    private final static int GAME_OVER = 106;
    private final static int RESET_DATA = 107;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_BLOCK_BOARD:
                    blockBoardAdapter.setColors(tempBlockBoardArray);
                    break;
                case REFRESH_NEXT_PIECE:
                    nextPieceAdapter.setColors(nextPieceArray);
                    break;
                case PAUSE_RESUME:
                    if (!govAnim.isRunning() && downTimer == null) {
                        downTimer = new Timer();
                        downTimer.schedule(getTimerTask(), timeInterval, timeInterval);
                        btnSpace.setEnabled(true);
                        btnUp.setEnabled(true);
                        btnLeft.setEnabled(true);
                        btnRight.setEnabled(true);
                        btnDown.setEnabled(true);
                    } else {
                        cancelSpaceTimer();
                        cancelDownTimer();
                        btnSpace.setEnabled(false);
                        btnUp.setEnabled(false);
                        btnLeft.setEnabled(false);
                        btnRight.setEnabled(false);
                        btnDown.setEnabled(false);
                    }
                    break;
                case PAUSE:
                    btnSpace.setEnabled(false);
                    btnUp.setEnabled(false);
                    btnLeft.setEnabled(false);
                    btnRight.setEnabled(false);
                    btnDown.setEnabled(false);
                    break;
                case RESUME:
                    btnSpace.setEnabled(true);
                    btnUp.setEnabled(true);
                    btnLeft.setEnabled(true);
                    btnRight.setEnabled(true);
                    btnDown.setEnabled(true);
                    break;
                case REFRESH_SCORE:
                    tvScore.setText(String.valueOf(score));
                    if (score <= 1000) {
                        level = 1;
                        timeInterval = 800;
                    } else if (score <= 2000) {
                        level = 2;
                        timeInterval = 750;
                    } else if (score <= 3000) {
                        level = 3;
                        timeInterval = 700;
                    } else if (score <= 5000) {
                        level = 4;
                        timeInterval = 650;
                    } else if (score <= 7500) {
                        level = 5;
                        timeInterval = 600;
                    } else if (score <= 10000) {
                        level = 6;
                        timeInterval = 550;
                    } else if (score <= 12500) {
                        level = 7;
                        timeInterval = 500;
                    } else if (score <= 15000) {
                        level = 8;
                        timeInterval = 450;
                    } else {
                        level = 9;
                        timeInterval = 400;
                    }
                    tvLevel.setText(String.valueOf(level));
                    break;
                case GAME_OVER:
                    cancelSpaceTimer();
                    cancelDownTimer();
                    llAnim.setVisibility(View.VISIBLE);
                    govAnim.start();
                    String recordList = ConfigSPUtils.getString(getApplication(), RECORDLIST);
                    if (!TextUtils.isEmpty(recordList)) {
                        Gson gson = new Gson();
                        RecordListBean recordListBean = gson.fromJson(recordList, RecordListBean.class);
                        List<RecordListBean.RecordBean> recordBeanList = recordListBean.getRecordBeanList();
                        RecordListBean.RecordBean recordBean = recordBeanList.get(0);
                        int lastScore = Integer.parseInt(recordBean.getScore());
                        if (score > lastScore) {
                            showNewRecordDialog(recordBeanList);
                        }
                    } else {
                        if (score > 0) {
                            showNewRecordDialog(null);
                        }
                    }
                    break;
                case RESET_DATA:
                    resetData();
                    break;
            }
        }
    };

    private void showNewRecordDialog(@Nullable final List<RecordListBean.RecordBean> recordBeanList) {
        NewRecordDialog.Builder builder = new NewRecordDialog.Builder(MainActivity.this, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                NewRecordDialog newRecordDialog = (NewRecordDialog) dialog;
                RecordListBean listBean = new RecordListBean();
                List<RecordListBean.RecordBean> beanList = new ArrayList<>();
                if (recordBeanList != null) {
                    beanList.addAll(recordBeanList);
                }
                RecordListBean.RecordBean bean = new RecordListBean.RecordBean();
                if (TextUtils.isEmpty(newRecordDialog.getUserName())) {
                    bean.setName(newRecordDialog.getUserNameHint());
                } else {
                    bean.setName(newRecordDialog.getUserName());
                }
                bean.setScore(String.valueOf(score));
                bean.setTime(String.valueOf(System.currentTimeMillis()));
                beanList.add(0, bean);
                listBean.setRecordBeanList(beanList);
                ConfigSPUtils.putString(getApplication(), RECORDLIST, new Gson().toJson(listBean));
            }
        }).setUserNameHint(R.string.user_name_hint).setScoreValue(String.valueOf(score));
        builder.builder().show();
    }

    private void initView() {
        gvBlockBoard = (GridView) findViewById(R.id.gv_block_board);
        tvScore = (LedTextView) findViewById(R.id.tv_score);
        tvLevel = (LedTextView) findViewById(R.id.tv_level);
        tvMaxScore = (LedTextView) findViewById(R.id.tv_max_score);
        gvNextPiece = (GridView) findViewById(R.id.gv_next_piece);
        btnPause = (ShadowImageView) findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(this);
        btnRecordList = (ShadowImageView) findViewById(R.id.btn_record_list);
        btnRecordList.setOnClickListener(this);
        btnRestart = (ShadowImageView) findViewById(R.id.btn_restart);
        btnRestart.setOnClickListener(this);
        btnSpace = (ShadowImageView) findViewById(R.id.btn_space);
        btnSpace.setOnClickListener(this);
        btnUp = (ShadowImageView) findViewById(R.id.btn_up);
        btnUp.setOnClickListener(this);
        btnLeft = (ShadowImageView) findViewById(R.id.btn_left);
        btnLeft.setOnClickListener(this);
        btnRight = (ShadowImageView) findViewById(R.id.btn_right);
        btnRight.setOnClickListener(this);
        btnDown = (ShadowImageView) findViewById(R.id.btn_down);
        btnDown.setOnClickListener(this);
        govAnim = (GameOverView) findViewById(R.id.gov_anim);
        llAnim = (LinearLayout) findViewById(R.id.ll_anim);
        tvUserName = (TextView) findViewById(R.id.tv_user_name);
    }

    private void initData() {
        govAnim.stop();
        llAnim.setVisibility(View.GONE);
        tvMaxScore.setText("0");
        String recordList = ConfigSPUtils.getString(getApplication(), RECORDLIST);
        if (!TextUtils.isEmpty(recordList)) {
            Gson gson = new Gson();
            RecordListBean recordListBean = gson.fromJson(recordList, RecordListBean.class);
            List<RecordListBean.RecordBean> recordBeanList = recordListBean.getRecordBeanList();
            RecordListBean.RecordBean recordBean = recordBeanList.get(0);
            tvUserName.setText(recordBean.getName());
            Integer lastScore = Integer.parseInt(recordBean.getScore());
            tvMaxScore.setText(String.valueOf(lastScore));
        }
        String stateStr = StateSPUtils.getString(getApplication(), STATEBEAN);
        if (TextUtils.isEmpty(stateStr)) {
            currentPiece = PieceFatory.createPiece();
            currentPiece.getSimplePieceArray();
            row = currentPiece.getInitalRow() - 1;
            culumn = currentPiece.getInitalCulumn();
            currentPieceArray = currentPiece.getPieceArray();
            nextPiece = PieceFatory.createPiece();
            nextPieceArray = nextPiece.getSimplePieceArray();
            blockBoardArray = new int[BOARDROW * BOARDCULUMN];
            tempBlockBoardArray = new int[BOARDROW * BOARDCULUMN];
            for (int i = 0; i < BOARDROW * BOARDCULUMN; i++) {
                blockBoardArray[i] = 0;
            }
            tempBlockBoardArray = Arrays.copyOf(blockBoardArray, BOARDROW * BOARDCULUMN);
            score = 0;
            level = 1;
        } else {
            Gson gson = new Gson();
            StateBean stateBean = gson.fromJson(stateStr, StateBean.class);
            row = stateBean.getRow();
            culumn = stateBean.getCulumn();
            currentPieceArray = stateBean.getCurrentPieceArray();
            nextPieceArray = stateBean.getNextPieceArray();
            blockBoardArray = stateBean.getBlockBoardArray();
            tempBlockBoardArray = stateBean.getTempBlockBoardArray();
            String currentShape = stateBean.getCurrentShape();
            int currentState = stateBean.getCurrentState();
            currentPiece = PieceFatory.createPiece(currentShape, currentState);
            String nextShape = stateBean.getNextShape();
            int nextState = stateBean.getNextState();
            nextPiece = PieceFatory.createPiece(nextShape, nextState);
            score = stateBean.getScore();
            level = stateBean.getLevel();
        }
        tvScore.setText(String.valueOf(score));
        tvLevel.setText(String.valueOf(level));
        nextPieceAdapter = new BlockAdapter();
        gvNextPiece.setAdapter(nextPieceAdapter);
        blockBoardAdapter = new BlockAdapter();
        gvBlockBoard.setAdapter(blockBoardAdapter);
        uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
        nextPieceAdapter.setColors(nextPieceArray);
        uiHandler.sendEmptyMessage(RESUME);
        downTimer = new Timer();
        downTimer.schedule(getTimerTask(), timeInterval, timeInterval);
    }

    private void resetData() {
        govAnim.stop();
        llAnim.setVisibility(View.GONE);
        score = 0;
        level = 1;
        tvScore.setText(String.valueOf(score));
        tvLevel.setText(String.valueOf(level));
        tvMaxScore.setText("0");
        String recordList = ConfigSPUtils.getString(getApplication(), RECORDLIST);
        if (!TextUtils.isEmpty(recordList)) {
            Gson gson = new Gson();
            RecordListBean recordListBean = gson.fromJson(recordList, RecordListBean.class);
            List<RecordListBean.RecordBean> recordBeanList = recordListBean.getRecordBeanList();
            RecordListBean.RecordBean recordBean = recordBeanList.get(0);
            tvUserName.setText(recordBean.getName());
            Integer lastScore = Integer.parseInt(recordBean.getScore());
            tvMaxScore.setText(String.valueOf(lastScore));
        }
        currentPiece = PieceFatory.createPiece();
        currentPiece.getSimplePieceArray();
        row = currentPiece.getInitalRow() - 1;
        culumn = currentPiece.getInitalCulumn();
        currentPieceArray = currentPiece.getPieceArray();
        nextPiece = PieceFatory.createPiece();
        nextPieceArray = nextPiece.getSimplePieceArray();
        blockBoardArray = new int[BOARDROW * BOARDCULUMN];
        tempBlockBoardArray = new int[BOARDROW * BOARDCULUMN];
        for (int i = 0; i < BOARDROW * BOARDCULUMN; i++) {
            blockBoardArray[i] = 0;
        }
        tempBlockBoardArray = Arrays.copyOf(blockBoardArray, BOARDROW * BOARDCULUMN);
        nextPieceAdapter = new BlockAdapter();
        gvNextPiece.setAdapter(nextPieceAdapter);
        blockBoardAdapter = new BlockAdapter();
        gvBlockBoard.setAdapter(blockBoardAdapter);
        uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
        nextPieceAdapter.setColors(nextPieceArray);
        uiHandler.sendEmptyMessage(RESUME);
        downTimer = new Timer();
        downTimer.schedule(getTimerTask(), timeInterval, timeInterval);
    }

    private TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(DOWN);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_pause:
                if (isStart) {
                    cancelSpaceTimer();
                    uiHandler.sendEmptyMessage(PAUSE_RESUME);
                }
                break;
            case R.id.btn_record_list:
                if (isStart) {
                    Intent intent = new Intent(this, RecordListActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.btn_restart:
                if (isStart) {
                    handler.sendEmptyMessage(RESTART);
                }
                break;
            case R.id.btn_space:
                if (!govAnim.isRunning()) {
                    btnSpace.setEnabled(false);
                    cancelDownTimer();
                    cancelSpaceTimer();
                    spaceTimer = new Timer();
                    spaceTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(DOWN);
                        }
                    }, 0, 20);
                }
                break;
            case R.id.btn_up:
                handler.sendEmptyMessage(UP);
                break;
            case R.id.btn_left:
                handler.sendEmptyMessage(LEFT);
                break;
            case R.id.btn_right:
                handler.sendEmptyMessage(RIGHT);
                break;
            case R.id.btn_down:
                handler.sendEmptyMessage(DOWN);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelSpaceTimer();
        cancelDownTimer();
        downTimer = new Timer();
        downTimer.schedule(getTimerTask(), timeInterval, timeInterval);
        uiHandler.sendEmptyMessage(RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelSpaceTimer();
        cancelDownTimer();
        uiHandler.sendEmptyMessage(PAUSE);
    }

    @Override
    protected void onStop() {
        StateBean stateBean = new StateBean();
        stateBean.setRow(row);
        stateBean.setCulumn(culumn);
        stateBean.setCurrentPieceArray(currentPieceArray);
        stateBean.setCurrentShape(currentPiece.getShape());
        stateBean.setCurrentState(currentPiece.getState());
        stateBean.setNextPieceArray(nextPieceArray);
        stateBean.setNextShape(nextPiece.getShape());
        stateBean.setNextState(nextPiece.getState());
        stateBean.setBlockBoardArray(blockBoardArray);
        stateBean.setTempBlockBoardArray(tempBlockBoardArray);
        stateBean.setLevel(level);
        stateBean.setScore(score);
        StateSPUtils.putString(getApplication(), STATEBEAN, new Gson().toJson(stateBean));
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cancelSpaceTimer();
        cancelDownTimer();
        handlerThread.quit();
        govAnim.stop();
        super.onDestroy();
    }

    private void cancelDownTimer() {
        if (downTimer != null) {
            downTimer.cancel();
            downTimer = null;
        }
    }

    private void cancelSpaceTimer() {
        if (spaceTimer != null) {
            spaceTimer.cancel();
            spaceTimer = null;
        }
    }
}
