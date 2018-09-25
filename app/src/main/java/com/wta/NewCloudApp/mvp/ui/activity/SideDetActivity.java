package com.wta.NewCloudApp.mvp.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jess.arms.di.component.AppComponent;
import com.jess.arms.http.imageloader.glide.GlideArms;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wta.NewCloudApp.R;
import com.wta.NewCloudApp.di.component.DaggerSideDetComponent;
import com.wta.NewCloudApp.di.module.SideDetModule;
import com.wta.NewCloudApp.mvp.contract.SideDetContract;
import com.wta.NewCloudApp.mvp.model.entity.Business;
import com.wta.NewCloudApp.mvp.presenter.SideDetPresenter;
import com.wta.NewCloudApp.uitls.PackageUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 周边商家详情
 */
public class SideDetActivity extends BaseLoadingActivity<SideDetPresenter> implements SideDetContract.View {


    @BindView(R.id.im_head)
    ImageView imHead;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.im_class)
    ImageView imClass;
    @BindView(R.id.tv_location)
    TextView tvLocation;
    @BindView(R.id.lat_head)
    RelativeLayout latHead;
    @BindView(R.id.im_store_01)
    RoundedImageView imStore01;
    @BindView(R.id.im_store_02)
    RoundedImageView imStore02;
    @BindView(R.id.im_store_03)
    RoundedImageView imStore03;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_type)
    TextView tvType;
    @BindView(R.id.tv_desc)
    TextView tvDesc;
    @BindView(R.id.lat_pics)
    RelativeLayout latPics;
    private Business business;
    ArrayList<String> urls = new ArrayList<>();

    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {
        DaggerSideDetComponent
                .builder()
                .appComponent(appComponent)
                .sideDetModule(new SideDetModule(this))
                .build()
                .inject(this);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_side_det;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mPresenter.getStoreDet(getIntent().getIntExtra("store_id", 0));
    }

    @Override
    public int setUIMode() {
        return UIMODE_TRANSPARENT_NOTALL;
    }

    public static void startDet(Activity activity, int storeID) {
        Intent intent = new Intent(activity, SideDetActivity.class);
        intent.putExtra("store_id", storeID);
        activity.startActivity(intent);
    }

    @OnClick({R.id.im_phone, R.id.lat_imgs, R.id.tv_location})
    public void startPhone(View view) {
        switch (view.getId()) {
            case R.id.im_phone:
                if (TextUtils.isEmpty(business.telephone)) {
                    showToast("该店铺未填写电话。");
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + business.telephone));
                startActivity(intent);
                break;
            case R.id.lat_imgs:
                PhotoViewActivity.startViewPhoto(this, urls);
                break;
            case R.id.tv_location:
//                Intent intentMap = new Intent("android.intent.action.VIEW",
//                        android.net.Uri.parse("androidamap://navi?sourceApplication=lefaner&lat=" + business.shop_address_x + "&lon=" + business.shop_address_y + "&dev=1&style=0"));
//                intentMap.setPackage("com.autonavi.minimap");
//                intentMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intentMap);
                if (PackageUtils.appIsInstalled(this, "com.autonavi.minimap")) {
                    Intent intentMap = new Intent();
                    intentMap.setAction(Intent.ACTION_VIEW);
                    intentMap.addCategory(Intent.CATEGORY_DEFAULT);
                    String url = "androidamap://route?sourceApplication=lefaner" +
                            "&dlat=" + business.shop_address_x +
                            "&dlon=" + business.shop_address_y +
                            "&dname=" + business.shop_name +
                            "&dev=0&t=0";
                    Uri uri = Uri.parse(url);
                    intentMap.setData(uri);
                    startActivity(intentMap);
                } else {
                    showToast("请先安装高德地图！");
                }
        }

    }

    @Override
    public void showStoreDet(Business business) {
        this.business = business;
        GlideArms.with(this).load(business.shop_doorhead).placeholder(R.mipmap.side_b_placeholder).into(imHead);
        tvName.setText(business.shop_name);
        GlideArms.with(this).load(business.level_img).into(imClass);
        tvLocation.setText(business.location_address);
        tvTime.setText(String.format("%s-%s", business.start_time, business.end_time));
        tvType.setText(business.type_name);
        if (TextUtils.isEmpty(business.introduction)) {
            tvDesc.setText("暂未填写店铺详情");
        } else {
            tvDesc.setText(business.introduction);
        }
    }

}
