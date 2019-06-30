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
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cn.xydzjnq.tetris.bean.RecordListBean;
import cn.xydzjnq.tetris.block.Block;
import cn.xydzjnq.tetris.block.BlockFatory;
import cn.xydzjnq.tetris.view.LedTextView;

import static cn.xydzjnq.tetris.Constant.CONFIG;
import static cn.xydzjnq.tetris.Constant.RECORDLIST;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "MainActivity";
    private GridView gvTetris;
    private LedTextView tvScore;
    private LedTextView tvLevel;
    private LedTextView tvMaxScore;
    /**
     * 下一个：
     */
    private TextView tvNextTetris;
    private GridView gvNextTetris;
    private Button btnPause;
    private Button btnRecordList;
    private Button btnRestart;
    private Button btnSpace;
    private Button btnUp;
    private Button btnLeft;
    private Button btnRight;
    private Button btnDown;
    private Block currentBlock;
    private ImageView ivAnim;
    private LinearLayout llAnim;
    private int row;
    private int culumn;
    private Block nextBlock;
    private int[] currentArrays;
    private int[] nextArrays;
    private int[] previousAllArrays;
    private int[] allArrays;
    private BlockAdapter nextAdapter;
    private BlockAdapter blockAdapter;
    private static int REFRESH_BLOCKS = 0;
    private static int REFRESH_NEXT_BLOCKS = 1;
    private static int INIT_DATA = 2;
    private static int INSTANT_REFRESH_BLOCKS = 3;
    private static int UP = 4;
    private static int LEFT = 5;
    private static int RIGHT = 6;
    private static int DOWN = 7;
    private static int PAUSE = 8;
    private static int RESUME = 9;
    private static int REFRESH_SCORE = 10;
    private static int GAME_OVER = 11;
    private boolean isInitial = true;

    private Timer timer;
    private Timer spaceTimer;
    private int timeInterval = 600;
    private int level = 1;
    private int score = 0;
    private int scoreStep = 100;
    private HandlerThread handlerThread;

    private Handler handler;
    private AnimationDrawable animationDrawable;

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    blockAdapter.setColors(allArrays);
                    break;
                case 1:
                    nextAdapter.setColors(nextArrays);
                    break;
                case 8:
                    btnSpace.setEnabled(false);
                    btnRecordList.setEnabled(false);
                    btnUp.setEnabled(false);
                    btnLeft.setEnabled(false);
                    btnRight.setEnabled(false);
                    btnDown.setEnabled(false);
                    break;
                case 9:
                    btnSpace.setEnabled(true);
                    btnRecordList.setEnabled(true);
                    btnUp.setEnabled(true);
                    btnLeft.setEnabled(true);
                    btnRight.setEnabled(true);
                    btnDown.setEnabled(true);
                    break;
                case 10:
                    tvScore.setText(String.valueOf(score));
                    if (score <= 1000) {
                        level = 1;
                        timeInterval = 600;
                    } else if (score <= 2000) {
                        level = 2;
                        timeInterval = 550;
                    } else if (score <= 5000) {
                        level = 3;
                        timeInterval = 500;
                    } else if (score <= 10000) {
                        level = 4;
                        timeInterval = 450;
                    } else {
                        level = 5;
                        timeInterval = 400;
                    }
                    tvLevel.setText(String.valueOf(level));
                    break;
                case 11:
                    if (!animationDrawable.isRunning()) {
                        if (spaceTimer != null) {
                            spaceTimer.cancel();
                            spaceTimer = null;
                        }
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        llAnim.setVisibility(View.VISIBLE);
                        animationDrawable.start();
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
                        score = 0;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        handlerThread = new HandlerThread(UUID.randomUUID().toString(), -2);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            private boolean isCollision() {
                allArrays = Arrays.copyOf(previousAllArrays, 180);
                int count = 0;
                for (int i = row; i > row - 4 && i > 0; i--) {
                    for (int j = culumn; j < culumn + 4; j++) {
                        if (i <= 18 && j >= 1 && j <= 10) {
                            if (currentArrays[(4 - (row - i) - 1) * 4 + (j - culumn)] != 0) {
                                if (allArrays[(i - 1) * 10 + j - 1] != 0) {
                                    if (row <= currentBlock.getInitalRow()) {
                                        uiHandler.sendEmptyMessage(GAME_OVER);
                                    }
                                    return true;
                                }
                                count++;
                            }
                        }
                    }
                }
                if (count == 4) {
                    return false;
                }
                if (row < 4) {
                    return false;
                }
                return true;
            }

            private void setAllArrays() {
                allArrays = Arrays.copyOf(previousAllArrays, 180);
                for (int i = row; i > row - 4 && i > 0; i--) {
                    for (int j = culumn; j < culumn + 4; j++) {
                        if (i <= 18 && j >= 1 && j <= 10) {
                            if (allArrays[(i - 1) * 10 + j - 1] == 0) {
                                allArrays[(i - 1) * 10 + j - 1] = currentArrays[(4 - (row - i) - 1) * 4 + (j - culumn)];
                            }
                        }
                    }
                }
            }

            private void touchBottom() {
                if (spaceTimer != null) {
                    spaceTimer.cancel();
                    spaceTimer = null;
                }
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                row--;
                setAllArrays();
                previousAllArrays = Arrays.copyOf(allArrays, 180);
                blockDispear();
            }

            private void blockDispear() {
                if (spaceTimer != null) {
                    spaceTimer.cancel();
                    spaceTimer = null;
                }
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                uiHandler.sendEmptyMessage(PAUSE);
                for (int i = 18; i >= 1; i--) {
                    int count = 0;
                    for (int j = 1; j <= 10; j++) {
                        if (allArrays[(i - 1) * 10 + j - 1] == 1) {
                            count++;
                        }
                    }
                    if (count == 10) {
                        int splashCount = 5;
                        for (int k = 0; k < splashCount; k++) {
                            for (int j = 1; j <= 10; j++) {
                                allArrays[(i - 1) * 10 + j - 1] = (k / 2 == 0) ? 0 : 1;
                            }
                            uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        for (int x = i; x >= 2; x--) {
                            for (int j = 1; j <= 10; j++) {
                                allArrays[(x - 1) * 10 + j - 1] = allArrays[(x - 2) * 10 + j - 1];
                            }
                        }
                        for (int x = 0; x < 10; x++) {
                            allArrays[x] = 0;
                        }
                        previousAllArrays = Arrays.copyOf(allArrays, 180);
                        uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                        score += scoreStep;
                        scoreStep += 20;
                        uiHandler.sendEmptyMessage(REFRESH_SCORE);
                        blockDispear();
                        break;
                    }
                }
                uiHandler.sendEmptyMessage(RESUME);
                row = currentBlock.getInitalRow();
                if (!animationDrawable.isRunning() && timer == null) {
                    timer = new Timer();
                    timer.schedule(getTimerTask(), timeInterval, timeInterval);
                }
                currentBlock = nextBlock;
                currentArrays = currentBlock.getShape();
                culumn = currentBlock.getInitalCulumn();
                nextBlock = BlockFatory.createBlock();
                nextArrays = nextBlock.getSimpleShape();
                scoreStep = 100;
                uiHandler.sendEmptyMessage(REFRESH_NEXT_BLOCKS);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                        break;
                    case 2:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initData();
                            }
                        });
                        break;
                    case 3:
                        if (isCollision()) {
                            touchBottom();
                        } else {
                            setAllArrays();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                        }
                        break;
                    case 4:
                        currentArrays = currentBlock.nextShape();
                        if (isCollision()) {
                            currentArrays = currentBlock.previousShape();
                        } else {
                            setAllArrays();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                        }
                        break;
                    case 5:
                        culumn--;
                        if (isCollision()) {
                            culumn++;
                        } else {
                            setAllArrays();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                        }
                        break;
                    case 6:
                        culumn++;
                        if (isCollision()) {
                            culumn--;
                        } else {
                            setAllArrays();
                            uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                        }
                        break;
                    case 7:
                        if (!animationDrawable.isRunning()) {
                            row++;
                            if (isCollision()) {
                                touchBottom();
                            } else {
                                setAllArrays();
                                uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
                            }
                        }
                        break;
                }
            }
        };
    }

    private void initView() {
        gvTetris = (GridView) findViewById(R.id.gv_tetris);
        tvScore = (LedTextView) findViewById(R.id.tv_score);
        tvLevel = (LedTextView) findViewById(R.id.tv_level);
        tvMaxScore = (LedTextView) findViewById(R.id.tv_max_score);
        tvNextTetris = (TextView) findViewById(R.id.tv_next_tetris);
        gvNextTetris = (GridView) findViewById(R.id.gv_next_tetris);
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
        ivAnim = (ImageView) findViewById(R.id.iv_anim);
        llAnim = (LinearLayout) findViewById(R.id.ll_anim);
    }

    private void initData() {
        animationDrawable = (AnimationDrawable) ivAnim.getBackground();
        animationDrawable.stop();
        llAnim.setVisibility(View.GONE);
        tvScore.setText("0");
        tvLevel.setText("1");
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
        nextAdapter = new BlockAdapter();
        gvNextTetris.setAdapter(nextAdapter);
        nextArrays = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        uiHandler.sendEmptyMessage(REFRESH_NEXT_BLOCKS);
        blockAdapter = new BlockAdapter();
        gvTetris.setAdapter(blockAdapter);
        allArrays = new int[180];
        previousAllArrays = new int[180];
        for (int i = 0; i < 180; i++) {
            allArrays[i] = 0;
        }
        previousAllArrays = Arrays.copyOf(allArrays, 180);
        uiHandler.sendEmptyMessage(REFRESH_BLOCKS);
        currentBlock = BlockFatory.createBlock();
        currentBlock.getSimpleShape();
        currentArrays = currentBlock.getShape();
        row = currentBlock.getInitalRow();
        culumn = currentBlock.getInitalCulumn();
        nextBlock = BlockFatory.createBlock();
        nextArrays = nextBlock.getSimpleShape();
        nextAdapter.setColors(nextArrays);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(getTimerTask(), timeInterval, timeInterval);
    }

    private TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(INSTANT_REFRESH_BLOCKS);
                row++;
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_pause:
                if (spaceTimer != null) {
                    spaceTimer.cancel();
                    spaceTimer = null;
                }
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(getTimerTask(), timeInterval, timeInterval);
                    uiHandler.sendEmptyMessage(RESUME);
                } else {
                    timer.cancel();
                    timer = null;
                    uiHandler.sendEmptyMessage(PAUSE);
                }
                break;
            case R.id.btn_record_list:
                break;
            case R.id.btn_restart:
                if (isInitial) {
                    resetBlocks();
                }
                break;
            case R.id.btn_space:
                if (spaceTimer != null) {
                    spaceTimer.cancel();
                    spaceTimer = null;
                }
                spaceTimer = new Timer();
                spaceTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(DOWN);
                    }
                }, 0, 50);
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

    private void resetBlocks() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isInitial = false;
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                for (int i = 18; i > 0; i--) {
                    try {
                        Thread.sleep(180);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int j = 1; j <= 10; j++) {
                        allArrays[(i - 1) * 10 + j - 1] = 1;
                    }
                    handler.sendEmptyMessage(REFRESH_BLOCKS);
                }
                for (int i = 1; i <= 18; i++) {
                    try {
                        Thread.sleep(180);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int j = 1; j <= 10; j++) {
                        allArrays[(i - 1) * 10 + j - 1] = 0;
                    }
                    handler.sendEmptyMessage(REFRESH_BLOCKS);
                }
                previousAllArrays = Arrays.copyOf(allArrays, 180);
                isInitial = true;
                handler.sendEmptyMessage(INIT_DATA);
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        if (spaceTimer != null) {
            spaceTimer.cancel();
            spaceTimer = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        handlerThread.quit();
        super.onDestroy();
    }
}
