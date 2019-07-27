package cn.xydzjnq.tetris;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;

import cn.xydzjnq.tetris.bean.RecordListBean;
import cn.xydzjnq.tetris.util.ConfigSPUtils;

import static cn.xydzjnq.tetris.util.ConfigSPUtils.RECORDLIST;

public class RecordListActivity extends BaseActivity {
    private RecyclerView rvRecordList;
    private RecordAdapter recordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        initView();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvRecordList.setLayoutManager(layoutManager);
        recordAdapter = new RecordAdapter();
        rvRecordList.setAdapter(recordAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.shape_default_devider));
        rvRecordList.addItemDecoration(dividerItemDecoration);
        String recordList = ConfigSPUtils.getString(getApplication(), RECORDLIST);
        if (!TextUtils.isEmpty(recordList)) {
            Gson gson = new Gson();
            RecordListBean recordListBean = gson.fromJson(recordList, RecordListBean.class);
            List<RecordListBean.RecordBean> recordBeanList = recordListBean.getRecordBeanList();
            recordAdapter.setList(recordBeanList);
        }
    }

    private void initView() {
        rvRecordList = (RecyclerView) findViewById(R.id.rv_record_list);
    }
}
