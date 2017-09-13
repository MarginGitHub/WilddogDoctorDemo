package com.zd.wilddogdoctordemo.ui.doctor.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.zd.wilddogdoctordemo.adapter.HistoryRecordListAdapter;
import com.zd.wilddogdoctordemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class HistoryRecordFragment extends BaseFragment {

    @BindView(R.id.history_record_container)
    RecyclerView mHistoryRecordContainer;
    @BindView(R.id.record_refresh_layout)
    SmartRefreshLayout mRecordRefreshLayout;
    Unbinder unbinder;
    private HistoryRecordListAdapter mRecordListAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_record_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        initViews();
        return view;
    }

    @Override
    protected void initViews() {
        setupContainer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    private void setupRefreshLayout() {
        mRecordRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        mRecordRefreshLayout.setEnableRefresh(false);
        mRecordRefreshLayout.setEnableLoadmore(true);
        mRecordRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                mRecordRefreshLayout.finishLoadmore();
            }
        });
    }

    private void setupContainer() {
        setupRefreshLayout();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mHistoryRecordContainer.setLayoutManager(linearLayoutManager);
        mRecordListAdapter = new HistoryRecordListAdapter();
        mHistoryRecordContainer.setAdapter(mRecordListAdapter);
    }

}
