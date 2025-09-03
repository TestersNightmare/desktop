package com.android.launcher3;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.android.launcher3.model.data.ItemInfo;
import com.thinkup.core.api.AdError;
import com.thinkup.core.api.TUAdInfo;
import com.thinkup.core.api.TUAdRevenueListener;
import com.thinkup.core.api.TUAdSourceStatusListener;
import com.thinkup.splashad.api.TUSplashAd;
import com.thinkup.splashad.api.TUSplashAdEZListener;
import com.thinkup.splashad.api.TUSplashAdExtraInfo;


import java.util.HashMap;
import java.util.Map;

public class TopOnSdkManager {
    private TopOnSdkManager(){}

    private TUSplashAd splashAd;
    private Boolean isShowSplash = false;

    //    private static String splashPlacementId = "n1gbc3vbjji2qa";
    private static void log(String str){android.util.Log.d("TopOn:",str);}

    private static class TopOnSdkManagerHolder{
        private static TopOnSdkManager instance = new TopOnSdkManager();
    }
    public static TopOnSdkManager getInstance(){
        return TopOnSdkManagerHolder.instance;
    }

    public void loadSplash(Launcher context, String placementId ,ViewGroup container){
        //
        log("开始加载开屏广告----------loadSplash（）");

        splashAd = new TUSplashAd(context, placementId, new TUSplashAdEZListener() {

            @Override
            public void onAdLoaded() {
                log("loadSplash------------onAdLoaded");
                //当前Activity处于前台时进行广告展示
//                if (inForeBackground) {
//                    //container大小至少占屏幕75%
//                    splashAd.show(this, container);
//                }
            }

            @Override
            public void onNoAdError(AdError adError) {
                //加载失败直接进入主界面
                log("loadSplash-----------onNoAdError, AdError:"+adError);
//                context.startActivitySafely(v,intent,item);
            }

            @Override
            public void onAdShow(TUAdInfo adInfo) {
                isShowSplash = true;
                log("loadSplash-----------onAdShow,TUAdInfo:"+adInfo);
                splashAd.loadAd();
            }

            @Override
            public void onAdClick(TUAdInfo adInfo) { log("splashAd-----------onAdClick,TUAdInfo:"+adInfo);}

            @Override
            public void onAdDismiss(TUAdInfo adInfo, TUSplashAdExtraInfo splashAdExtraInfo) {
                log("loadSplash-----------onAdDismiss,TUAdInfo:"+adInfo+"; TUSplashAdExtraInfo:"+splashAdExtraInfo);
                isShowSplash=false;
                if (container != null) {
                    container.removeAllViews();
                    container.setVisibility(View.GONE);
                }
                if (v!=null && intent!=null && item!=null){
                    Window window = context.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setNavigationBarColor(ContextCompat.getColor(context, R.color.transparent));
                    context.startActivitySafely(v,intent,item);
                    v = null;
                    intent = null;
                    item = null;
                }
                // 热启开屏推荐进行pre-load
                splashAd.loadAd();
                //开屏广告展示关闭后进入主界面

            }
        }, 5000); // 注：xxx需要替换为您的开屏广告超时时间，单位：毫秒
        Map<String, Object> localMap = new HashMap<>();
        splashAd.setLocalExtra(localMap);
        splashAd.setAdSourceStatusListener(new TUAdSourceStatusListener() {
            @Override
            public void onAdSourceBiddingAttempt(TUAdInfo tuAdInfo) {
                log("setAdSourceStatusListener-----------onAdSourceBiddingAttempt, TUAdInfo:"+tuAdInfo);
            }

            @Override
            public void onAdSourceBiddingFilled(TUAdInfo tuAdInfo) {
                log("setAdSourceStatusListener-----------onAdSourceBiddingFilled, TUAdInfo:"+tuAdInfo);
            }

            @Override
            public void onAdSourceBiddingFail(TUAdInfo tuAdInfo, AdError adError) {
                log("setAdSourceStatusListener-----------onAdSourceBiddingFail, TUAdInfo:"+tuAdInfo+";  AdError:"+adError);
            }

            @Override
            public void onAdSourceAttempt(TUAdInfo tuAdInfo) {
                log("setAdSourceStatusListener-----------onAdSourceAttempt, TUAdInfo:"+tuAdInfo);
            }

            @Override
            public void onAdSourceLoadFilled(TUAdInfo tuAdInfo) {
                log("setAdSourceStatusListener-----------onAdSourceLoadFilled, TUAdInfo:"+tuAdInfo);
            }

            @Override
            public void onAdSourceLoadFail(TUAdInfo tuAdInfo, AdError adError) {
                log("setAdSourceStatusListener-----------onAdSourceLoadFail, TUAdInfo:"+tuAdInfo+";  AdError:"+adError);
            }
        });
        splashAd.setAdRevenueListener(new TUAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(TUAdInfo tuAdInfo) {
                log("setAdRevenueListener-----------onAdRevenuePaid, TUAdInfo:"+tuAdInfo);
            }
        });
        splashAd.loadAd();
    }
    private View v;
    private Intent intent;
    private ItemInfo item;

    public Boolean showSplash(Activity context, ViewGroup container, View v, Intent intent, ItemInfo item){
        if(splashAd.isAdReady() && !isShowSplash){
            log("isAdReady   true && isShowSplash  false");
            if (container !=null){
                container.setVisibility(View.VISIBLE);
            }
            isShowSplash = true;
            this.v = v;
            this.intent = intent;
            this.item = item;
            //container大小至少占屏幕75%
            splashAd.show(context, container);
            return true;
        }else{
            isShowSplash = false;
            //重新加载
            splashAd.loadAd();
            return false;
        }
    }


}
