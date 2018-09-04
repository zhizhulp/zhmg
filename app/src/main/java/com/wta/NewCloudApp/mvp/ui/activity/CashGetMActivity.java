package com.wta.NewCloudApp.mvp.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jess.arms.base.delegate.IFragment;
import com.jess.arms.di.component.AppComponent;
import com.wta.NewCloudApp.R;
import com.wta.NewCloudApp.di.component.DaggerCashGetComponent;
import com.wta.NewCloudApp.di.module.CashGetModule;
import com.wta.NewCloudApp.mvp.contract.CashGetContract;
import com.wta.NewCloudApp.mvp.model.entity.Bill;
import com.wta.NewCloudApp.mvp.model.entity.Result;
import com.wta.NewCloudApp.mvp.model.entity.Score;
import com.wta.NewCloudApp.mvp.presenter.CashGetPresenter;
import com.wta.NewCloudApp.mvp.ui.adapter.CashGetAdapter;
import com.wta.NewCloudApp.uitls.DialogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 收款记录界面
 */
public class CashGetMActivity extends BaseListActivity<CashGetPresenter> implements CashGetContract.View {

    @BindView(R.id.tv_month)
    TextView tvMonth;
    @BindView(R.id.tv_switch)
    TextView tvSwitch;
    @BindView(R.id.tv_yang)
    TextView tvYang;
    @BindView(R.id.tv_total_money)
    TextView tvTotalMoney;
    @BindView(R.id.tv_total_count)
    TextView tvTotalCount;
    @BindView(R.id.tv_total_score)
    TextView tvTotalScore;
    @BindView(R.id.lat_head)
    View latHead;
    private int monthOrDay;//0日账单 1月账单
    private String date;

    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {
        DaggerCashGetComponent
                .builder()
                .appComponent(appComponent)
                .cashGetModule(new CashGetModule(this))
                .build()
                .inject(this);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_cash_get;
    }

    public static void startCashList(Activity activity, int type) {
        Intent intent = new Intent(activity, CashGetMActivity.class);
        intent.putExtra("type", type);
        activity.startActivity(intent);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        monthOrDay = getIntent().getIntExtra("type", 0);//记住了
        if (monthOrDay == 0) {
            date = formateTime(new Date(), "yyyy-MM-dd");
        } else if (monthOrDay == 1) {
            date = formateTime(new Date(), "yyyy-MM");
        }
        tvMonth.setText(date);
        super.initData(savedInstanceState);
        if (monthOrDay == 0) {
            Drawable drawable = getResources().getDrawable(R.mipmap.calendar_day);
            drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            tvMonth.setCompoundDrawables(drawable, null, null, null);
            tvSwitch.setText("月收益");
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        adapter.setHeaderAndEmpty(true);
        adapter.setEmptyView(R.layout.cash_get_empty);
    }

    @Override
    protected void getAdapter() {
        adapter = new CashGetAdapter(R.layout.b_cash_item, data);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                CashDetActivity.startCashDet(CashGetMActivity.this, ((Bill) data.get(position)).bill_id);
            }
        });
    }

    @OnClick({R.id.tv_month, R.id.tv_switch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_month:
                if (monthOrDay == 0) {
                    DialogUtils.showDayTimePicker(this, new OnTimeSelectListener() {
                        @Override
                        public void onTimeSelect(Date date, View v) {
                            isRefresh = true;
                            CashGetMActivity.this.date = formateTime(date, "yyyy-MM-dd");
                            getData(isRefresh);
                        }
                    }).show();
                } else if (monthOrDay == 1) {
                    DialogUtils.showMonthTimePicker(this, new OnTimeSelectListener() {
                        @Override
                        public void onTimeSelect(Date date, View v) {
                            isRefresh = true;
                            CashGetMActivity.this.date = formateTime(date, "yyyy-MM");
                            getData(isRefresh);
                        }
                    }).show();
                }
                break;
            case R.id.tv_switch:
                if (monthOrDay == 1) {
                    finish();
                } else if (monthOrDay == 0) {
                    CashGetMActivity.startCashList(this, 1);
                }
                break;
        }
    }

    @Override
    public void loadData(boolean isRefresh) {
        getData(isRefresh);
    }

    private void getData(boolean isRefresh) {
        mPresenter.getBReceiveList(isRefresh, 1, monthOrDay == 0 ? "day" : "month", date);
    }

    @Override
    public void getData(int what, Result<List> result) {
        List msgs = result.data;
        if (!isRefresh && (msgs == null || msgs.size() == 0))
            isComplete = true;
        if (isRefresh) {
            data.clear();
            if (msgs != null && msgs.size() > 0) {
                data.addAll(msgs);
                //latHead.setVisibility(View.VISIBLE);
                tvTotalMoney.setText(result.sumNumber.integralIncome + "");
                tvTotalCount.setText(result.sumNumber.receivablesNumber + "");
                tvTotalScore.setText(result.sumNumber.sumProfit + "");
                adapter.notifyDataSetChanged();
            } else {
                //latHead.setVisibility(View.GONE);
            }
        } else {
            int beforeSize = data.size();
            if (msgs != null) {
                data.addAll(msgs);
                adapter.notifyItemRangeInserted(beforeSize, msgs.size());
            }
        }
    }

    private String formateTime(Date date, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }
}
