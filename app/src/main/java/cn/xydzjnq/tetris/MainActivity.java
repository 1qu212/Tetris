package cn.xydzjnq.tetris;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.xydzjnq.tetris.bean.RecordListBean;
import cn.xydzjnq.tetris.piece.Piece;
import cn.xydzjnq.tetris.piece.PieceFatory;
import cn.xydzjnq.tetris.view.GameOverView;
import cn.xydzjnq.tetris.view.LedTextView;

import static cn.xydzjnq.tetris.Constant.CONFIG;
import static cn.xydzjnq.tetris.Constant.RECORDLIST;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "MainActivity";
    private GridView gvBlockBoard;
    private LedTextView tvScore;
    private LedTextView tvLevel;
    private LedTextView tvMaxScore;
    private GridView gvNextPiece;
    private Button btnPause;
    private Button btnRecordList;
    private Button btnRestart;
    private Button btnSpace;
    private Button btnUp;
    private Button btnLeft;
    private Button btnRight;
    private Button btnDown;
    private Piece currentPiece;
    private GameOverView govAnim;
    private LinearLayout llAnim;
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
//    private AnimationDrawable animationDrawable;

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
                            for (int j = 1; j <= BOARDCULUMN; j++) {
                                tempBlockBoardArray[(x - 1) * BOARDCULUMN + j - 1] = tempBlockBoardArray[(x - 2) * BOARDCULUMN + j - 1];
                            }
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
                        btnRecordList.setEnabled(true);
                        btnUp.setEnabled(true);
                        btnLeft.setEnabled(true);
                        btnRight.setEnabled(true);
                        btnDown.setEnabled(true);
                    } else {
                        cancelSpaceTimer();
                        cancelDownTimer();
                        downTimer = null;
                        btnSpace.setEnabled(false);
                        btnRecordList.setEnabled(false);
                        btnUp.setEnabled(false);
                        btnLeft.setEnabled(false);
                        btnRight.setEnabled(false);
                        btnDown.setEnabled(false);
                    }
                    break;
                case PAUSE:
                    cancelSpaceTimer();
                    cancelDownTimer();
                    downTimer = null;
                    btnSpace.setEnabled(false);
                    btnRecordList.setEnabled(false);
                    btnUp.setEnabled(false);
                    btnLeft.setEnabled(false);
                    btnRight.setEnabled(false);
                    btnDown.setEnabled(false);
                    break;
                case RESUME:
                    downTimer = new Timer();
                    downTimer.schedule(getTimerTask(), timeInterval, timeInterval);
                    btnSpace.setEnabled(true);
                    btnRecordList.setEnabled(true);
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
                    SharedPreferences sharedPreferences = getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
                    String recordList = sharedPreferences.getString(RECORDLIST, "");
                    if (!recordList.isEmpty()) {
                        Gson gson = new Gson();
                        RecordListBean recordListBean = gson.fromJson(recordList, RecordListBean.class);
                        List<RecordListBean.RecordBean> recordBeanList = recordListBean.getRecordBeanList();
                        int size = recordBeanList.size();
                        RecordListBean.RecordBean recordBean = recordBeanList.get(size - 1);
                        Integer lastScore = Integer.parseInt(recordBean.getScore());
                        if (score >= lastScore) {
                            RecordListBean listBean = new RecordListBean();
                            List<RecordListBean.RecordBean> beanList = new ArrayList<>();
                            beanList.addAll(recordBeanList);
                            RecordListBean.RecordBean bean = new RecordListBean.RecordBean();
                            bean.setName("匿名");
                            bean.setScore(String.valueOf(score));
                            bean.setTime(String.valueOf(System.currentTimeMillis()));
                            beanList.add(bean);
                            listBean.setRecordBeanList(beanList);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(RECORDLIST, new Gson().toJson(listBean));
                            editor.commit();
                        }
                    } else {
                        if (score > 0) {
                            RecordListBean recordListBean = new RecordListBean();
                            RecordListBean.RecordBean recordBean = new RecordListBean.RecordBean();
                            recordBean.setName("匿名");
                            recordBean.setScore(String.valueOf(score));
                            recordBean.setTime(String.valueOf(System.currentTimeMillis()));
                            List<RecordListBean.RecordBean> recordBeanList = new ArrayList<>();
                            recordBeanList.add(recordBean);
                            recordListBean.setRecordBeanList(recordBeanList);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(RECORDLIST, new Gson().toJson(recordListBean));
                            editor.commit();
                        }
                    }
                    break;
                case RESET_DATA:
                    initData();
                    break;
            }
        }
    };

    private void initView() {
        gvBlockBoard = (GridView) findViewById(R.id.gv_block_board);
        tvScore = (LedTextView) findViewById(R.id.tv_score);
        tvLevel = (LedTextView) findViewById(R.id.tv_level);
        tvMaxScore = (LedTextView) findViewById(R.id.tv_max_score);
        gvNextPiece = (GridView) findViewById(R.id.gv_next_piece);
        btnPause = (Button) findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(this);
        btnRecordList = (Button) findViewById(R.id.btn_record_list);
        btnRecordList.setOnClickListener(this);
        btnRestart = (Button) findViewById(R.id.btn_restart);
        btnRestart.setOnClickListener(this);
        btnSpace = (Button) findViewById(R.id.btn_space);
        btnSpace.setOnClickListener(this);
        btnUp = (Button) findViewById(R.id.btn_up);
        btnUp.setOnClickListener(this);
        btnLeft = (Button) findViewById(R.id.btn_left);
        btnLeft.setOnClickListener(this);
        btnRight = (Button) findViewById(R.id.btn_right);
        btnRight.setOnClickListener(this);
        btnDown = (Button) findViewById(R.id.btn_down);
        btnDown.setOnClickListener(this);
        govAnim = (GameOverView) findViewById(R.id.gov_anim);
        llAnim = (LinearLayout) findViewById(R.id.ll_anim);
    }

    private void initData() {
        govAnim.stop();
        llAnim.setVisibility(View.GONE);
        score = 0;
        level = 1;
        tvScore.setText(String.valueOf(score));
        tvLevel.setText(String.valueOf(level));
        tvMaxScore.setText("0");
        SharedPreferences sharedPreferences = getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        String recordList = sharedPreferences.getString(RECORDLIST, "");
        if (!recordList.isEmpty()) {
            Gson gson = new Gson();
            RecordListBean recordListBean = gson.fromJson(recordList, RecordListBean.class);
            List<RecordListBean.RecordBean> recordBeanList = recordListBean.getRecordBeanList();
            int size = recordBeanList.size();
            RecordListBean.RecordBean recordBean = recordBeanList.get(size - 1);
            Integer lastScore = Integer.parseInt(recordBean.getScore());
            tvMaxScore.setText(String.valueOf(lastScore));
        }
        nextPieceAdapter = new BlockAdapter();
        gvNextPiece.setAdapter(nextPieceAdapter);
        blockBoardAdapter = new BlockAdapter();
        gvBlockBoard.setAdapter(blockBoardAdapter);
        tempBlockBoardArray = new int[BOARDROW * BOARDCULUMN];
        blockBoardArray = new int[BOARDROW * BOARDCULUMN];
        for (int i = 0; i < BOARDROW * BOARDCULUMN; i++) {
            tempBlockBoardArray[i] = 0;
        }
        blockBoardArray = Arrays.copyOf(tempBlockBoardArray, BOARDROW * BOARDCULUMN);
        uiHandler.sendEmptyMessage(REFRESH_BLOCK_BOARD);
        currentPiece = PieceFatory.createPiece();
        currentPiece.getSimplePieceArray();
        currentPieceArray = currentPiece.getPieceArray();
        row = currentPiece.getInitalRow() - 1;
        culumn = currentPiece.getInitalCulumn();
        nextPiece = PieceFatory.createPiece();
        nextPieceArray = nextPiece.getSimplePieceArray();
        nextPieceAdapter.setColors(nextPieceArray);
        uiHandler.sendEmptyMessage(RESUME);
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
                break;
            case R.id.btn_restart:
                if (isStart) {
                    handler.sendEmptyMessage(RESTART);
                }
                break;
            case R.id.btn_space:
                if (!govAnim.isRunning()) {
                    btnSpace.setEnabled(false);
                    cancelSpaceTimer();
                    spaceTimer = new Timer();
                    spaceTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(DOWN);
                        }
                    }, 50, 50);
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
