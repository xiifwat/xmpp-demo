package com.xiifwat.xmppdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.jxmpp.stringprep.XmppStringprepException;


public class XmppService extends Service {

    private String username;
    private String password;
    private ChatManager xmpp;

    public XmppService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStartCommand(intent, START_FLAG_REDELIVERY, 1);
        return new LocalBinder<>(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("pwd")) {
            username = intent.getStringExtra("user");
            password = intent.getStringExtra("pwd");

            try {
                if (xmpp == null)
                    xmpp = ChatManager.getInstance();
                xmpp.init(username, password);
                xmpp.connectConnection();
            } catch (XmppStringprepException e) {
                Log.e("XmppService", "XmppService:onStartCommand", e);
            }
        } else if (intent != null) {
            if (xmpp != null) {
                xmpp.logout();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(xmpp!=null) xmpp.disconnectConnection();
        super.onDestroy();
    }


    public ChatManager getXmpp() {
        return xmpp;
    }
}
