package com.xiifwat.xmppdemo;

/**
 * Created by Tawfiq on 03/10/2019
 */
import android.os.Binder;

import java.lang.ref.WeakReference;

public class LocalBinder <S> extends Binder {
    private final WeakReference<S> mService;

    public LocalBinder(final S service) {
        mService = new WeakReference<S>(service);
    }

    public S getService() {
        return mService.get();
    }

}