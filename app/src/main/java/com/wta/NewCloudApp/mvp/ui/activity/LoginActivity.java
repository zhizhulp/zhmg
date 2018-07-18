package com.wta.NewCloudApp.mvp.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.jess.arms.di.component.AppComponent;
import com.jess.arms.integration.lifecycle.Lifecycleable;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.RxLifecycleUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.wta.NewCloudApp.config.App;
import com.wta.NewCloudApp.config.AppConfig;
import com.wta.NewCloudApp.di.component.DaggerLoginComponent;
import com.wta.NewCloudApp.di.module.LoginModule;
import com.wta.NewCloudApp.jiuwei210278.R;
import com.wta.NewCloudApp.mvp.contract.LoginContract;
import com.wta.NewCloudApp.mvp.model.entity.LoginEntity;
import com.wta.NewCloudApp.mvp.model.entity.Result;
import com.wta.NewCloudApp.mvp.model.entity.TabWhat;
import com.wta.NewCloudApp.mvp.model.entity.User;
import com.wta.NewCloudApp.mvp.presenter.LoginPresenter;
import com.wta.NewCloudApp.mvp.ui.widget.ClearEditText;
import com.wta.NewCloudApp.mvp.ui.widget.EditTextHint;
import com.wta.NewCloudApp.uitls.RegexUtils;

import org.simple.eventbus.EventBus;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class LoginActivity extends BaseLoadingActivity<LoginPresenter> implements LoginContract.View, UMAuthListener {

    @BindView(R.id.im_icon)
    ImageView imIcon;
    @BindView(R.id.et_phone)
    ClearEditText etPhone;
    @BindView(R.id.et_code)
    EditTextHint etCode;
    @BindView(R.id.tv_get_code)
    TextView tvGetCode;
    @BindView(R.id.et_recommend_code)
    ClearEditText etRecommendCode;
    @BindView(R.id.checkBox)
    CheckBox checkBox;
    @BindView(R.id.tv_agree)
    TextView tvAgree;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.im_wx_login)
    ImageView imWxLogin;
    private static int CODE_TIME = 30;
    @BindView(R.id.rec_line)
    View recLine;

    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {
        DaggerLoginComponent //如找不到该类,请编译一下项目
                .builder()
                .appComponent(appComponent)
                .loginModule(new LoginModule(this))
                .build()
                .inject(this);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_login; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        setAgreeVisible(AppConfig.getInstance().getBoolean("is_login", false) ? 1 : 0);
        final boolean[] etPhoneEnable = {false};
        final boolean[] etCodeEnable = {false};
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                etPhoneEnable[0] = (s.length() > 0);
                if (etCodeEnable[0] || etCodeEnable[0]) btnLogin.setEnabled(true);
                else btnLogin.setEnabled(false);
            }
        });
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                etCodeEnable[0] = (s.length() > 0);
                if (etCodeEnable[0] || etCodeEnable[0]) btnLogin.setEnabled(true);
                else btnLogin.setEnabled(false);
            }
        });
    }

    @Override
    public LoginActivity getActivity() {
        return this;
    }

    @OnClick({R.id.tv_get_code, R.id.btn_login, R.id.tv_agree, R.id.im_wx_login})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.tv_get_code:
                String s = etPhone.getText().toString();
                if (checkError(s)) {
                    etCode.requestFocus();
                    mPresenter.getCode(s);
                }
                break;
            case R.id.btn_login:
                if (checkLoginEnable()) {
                    mPresenter.login(etPhone.getText().toString(), etCode.getText().toString(), etRecommendCode.getText().toString());
                }
                break;
            case R.id.tv_agree:
                WebViewActivity.start(this, "用户协议", "http://www.baidu.com");
                break;
            case R.id.im_wx_login:
                if (UMShareAPI.get(this).isInstall(this, SHARE_MEDIA.WEIXIN))
                    UMShareAPI.get(this).getPlatformInfo(this, SHARE_MEDIA.WEIXIN, this);
                else ArmsUtils.makeText(App.getInstance(), "您没有安装微信");
                break;
        }
    }

    private boolean checkLoginEnable() {
        if (RegexUtils.isMobile(etPhone.getText().toString()) && etCode.length() > 0) return true;
        if (!RegexUtils.isMobile(etPhone.getText().toString())) {
            ArmsUtils.makeText(this.getApplicationContext(), "输入不符合规范，请重新输入");
            return false;
        }
        if (etCode.length() == 0) {
            ArmsUtils.makeText(this.getApplicationContext(), "请输入验证码");
            return false;
        }
        if (checkBox.isEnabled() && !checkBox.isChecked()) {
            ArmsUtils.makeText(this.getApplicationContext(), "请阅读并同意协议");
            return false;
        }
        return false;
    }

    public boolean checkError(String msg) {
        if (msg.length() != 0)
            if (RegexUtils.isMobile(msg)) return true;
            else ArmsUtils.makeText(this.getApplicationContext(), "手机号码输入不符合规范，请重新输入");
        else ArmsUtils.makeText(this.getApplicationContext(), "请输入11位手机号码");
        return false;
    }

    @Override
    public void timeCutDown(Result<User> results) {
        ArmsUtils.makeText(getApplicationContext(), "短信验证码已发送");
        setAgreeVisible(results.data.is_member);
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(RxLifecycleUtils.bindToLifecycle((Lifecycleable) this))
                .subscribeOn(Schedulers.io())
                .take(CODE_TIME, TimeUnit.SECONDS)
                .doOnSubscribe(disposable -> tvGetCode.setEnabled(false))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(aLong -> CODE_TIME - aLong)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNext(Long aLong) {
                        tvGetCode.setText(aLong + "s");
                    }

                    @Override
                    public void onError(Throwable e) {
                        tvGetCode.setEnabled(true);
                        tvGetCode.setText("获取验证码");
                    }

                    @Override
                    public void onComplete() {
                        tvGetCode.setEnabled(true);
                        tvGetCode.setText("获取验证码");
                    }
                });
    }

    /**
     * 设置协议是否显示
     */
    private void setAgreeVisible(int is_member) {
        checkBox.setVisibility(is_member == 0 ? View.VISIBLE : View.GONE);
        tvAgree.setVisibility(is_member == 0 ? View.VISIBLE : View.GONE);
        recLine.setVisibility(is_member == 0 ? View.VISIBLE : View.GONE);
        etRecommendCode.setVisibility(is_member == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart(SHARE_MEDIA share_media) {
        showLoading();
    }

    @Override
    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
        hideLoading();
        mPresenter.wxLogin(map);
    }

    @Override
    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
        hideLoading();
    }

    @Override
    public void onCancel(SHARE_MEDIA share_media, int i) {
        ArmsUtils.makeText(this, "微信登陆已取消");
    }

    @Override
    public void loginSuccess(Result<LoginEntity> results) {
        Timber.d("loginSuccess: ");
        ArmsUtils.makeText(getApplicationContext(), results.msg);
        if (!AppConfig.getInstance().getBoolean("is_login", false))
            EventBus.getDefault().post(new TabWhat(2));
        else
            EventBus.getDefault().post(1);
        //用于判断是否显示注册协议,点击主页面第三个tab的判断
        AppConfig.getInstance().putBoolean("is_login", true);
        //save user
        User user = results.data.user_info;
        AppConfig.getInstance().putUser(user);
        //save session
        AppConfig.getInstance().putString("sessionId",results.data.update_token.sessionId);
        AppConfig.getInstance().putString("accessToken",results.data.update_token.accessToken);
        finish();
    }
}