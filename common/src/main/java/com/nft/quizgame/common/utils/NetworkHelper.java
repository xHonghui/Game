package com.nft.quizgame.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

public class NetworkHelper {

    public static final String NET_WORK_BROAD_CAST_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    //description:参数设置
    private int netState = -1;
    private static boolean preNetworkStateIsNone = false;

    //description:网络监听
    private BroadcastReceiver mNetWorkChangeReceiver;
    private IntentFilter intentFilter;
    private boolean isRegister = false;
    private NetWorkChangeListener netWorkChangeListener;
    private WeakReference<Context> contextWeakReference;

    public NetworkHelper(Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    /**
     * 初始化工具类
     */
    public void init() {
        mNetWorkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                netState = NetworkUtils.getNetworkState(context);
                if (netState == NetworkUtils.NETWORK_STATE_NONE) {
                    if (netWorkChangeListener != null) {
                        netWorkChangeListener.onNetWorkChange(netState, false, preNetworkStateIsNone);
                        preNetworkStateIsNone = true;
                    }
                } else {
                    if (netWorkChangeListener != null) {
                        netWorkChangeListener.onNetWorkChange(netState, true, preNetworkStateIsNone);
                        preNetworkStateIsNone = false;
                    }
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(NET_WORK_BROAD_CAST_ACTION);
        Context context = contextWeakReference.get();
        if (context != null) {
            context.registerReceiver(mNetWorkChangeReceiver, intentFilter);
        }
        isRegister = true;
    }

    public void release() {
        try {
            Context context = contextWeakReference.get();
            if (context != null) {
                context.unregisterReceiver(mNetWorkChangeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        netWorkChangeListener = null;
    }

    public interface NetWorkChangeListener {
        /**
         * 网络监听回调，仅有断网和网络正常的情况
         *
         * @param netWorkState 当前网络状态下的TAG
         * @param isConnective 是否可用
         */
        void onNetWorkChange(int netWorkState, boolean isConnective, boolean isLost);
    }

    public void setNetWorkChangeListener(NetWorkChangeListener netWorkChangeListener) {
        this.netWorkChangeListener = netWorkChangeListener;
    }

}
