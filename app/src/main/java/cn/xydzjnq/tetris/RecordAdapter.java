package cn.xydzjnq.tetris;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.xydzjnq.tetris.bean.RecordListBean;
import cn.xydzjnq.tetris.util.TimeUtils;

public class RecordAdapter<T> extends RecyclerView.Adapter {
    private List<T> list;

    public RecordAdapter() {
    }

    public void setList(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_record, viewGroup, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        RecordViewHolder holder = (RecordViewHolder) viewHolder;
        RecordListBean.RecordBean recordBean = (RecordListBean.RecordBean) list.get(i);
        holder.tvScore.setText(recordBean.getScore());
        holder.tvUserName.setText(recordBean.getName());
        holder.tvTime.setText(TimeUtils.getDefaultTime(Long.parseLong(recordBean.getTime())));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvScore;
        TextView tvUserName;
        TextView tvTime;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
