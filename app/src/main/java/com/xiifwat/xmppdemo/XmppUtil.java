package com.xiifwat.xmppdemo;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class XmppUtil {

    static InetAddress getHostIp(String mHost) {
        InetAddress returnValue;
        final String host;

        if(mHost.startsWith("http://")) {
            host = mHost.replaceFirst("http://", "");
        } else if(mHost.startsWith("https://")) {
            host = mHost.replaceFirst("https://", "");
        } else {
            host = mHost;
        }

        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<InetAddress> result = es.submit(() -> {
            // executing on worker thread
            try {
                InetAddress ip = InetAddress.getByName(host);
                Log.i("xmpp", host + " to IP conversion success");
                return ip;
            } catch (UnknownHostException e) {
                Log.e("xmpp", host + " to IP conversion failed", e);
                return null;
            }
        });

        try {
            returnValue = result.get();
        } catch (Exception e) {
            returnValue = null; // failed
        } finally {
            es.shutdown();
        }

        return returnValue;
    }
}
