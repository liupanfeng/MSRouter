package com.meishe.ms_common;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 跳转时 ，用于参数的传递
 */
public class MSBundleManager {

    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return this.bundle;
    }

    // 对外界提供，可以携带参数的方法
    public MSBundleManager withString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this;
    }


    public MSBundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public MSBundleManager withInt(@NonNull String key, @Nullable int value) {
        bundle.putInt(key, value);
        return this;
    }

    public MSBundleManager withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    /**
     * 按照单一职责  这里只负责参数解析  并不负责跳转
     * @param context
     * @return
     */
    public Object navigation(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return MSRouterManager.getInstance().navigation(context, this);
        }
        return null;
    }


}
