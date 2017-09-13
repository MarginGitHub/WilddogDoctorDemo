package com.zd.wilddogdoctordemo.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


import java.util.List;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class HistoryRecordListAdapter extends RecyclerView.Adapter<HistoryRecordListAdapter.HistoryRecordViewHolder> {

    private List<Object> mData;

    public void setData(List<Object> data) {
        mData = data;
    }

    @Override
    public HistoryRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryRecordViewHolder(null);
    }

    @Override
    public void onBindViewHolder(HistoryRecordViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    class HistoryRecordViewHolder extends RecyclerView.ViewHolder {

        public HistoryRecordViewHolder(View itemView) {
            super(itemView);
        }
    }

}
