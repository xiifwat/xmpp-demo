package com.xiifwat.xmppdemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.jxmpp.stringprep.XmppStringprepException;

/**
 * Created by Tawfiq on 03/10/2019
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private XmppService mService;
    private View view;
    private boolean mBounded;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(final ComponentName name,
                                       final IBinder service) {
            mService = ((LocalBinder<XmppService>) service).getService();
            mBounded = true;
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
            mBounded = false;
            Log.d(TAG, "onServiceDisconnected");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //Click Handler for Login Button
    public void onClickLoginBtn(View view) {
        try {
            EditText userId = findViewById(R.id.txtUser);
            EditText userPwd = findViewById(R.id.txtPwd);
            String userName = userId.getText().toString().trim();
            String passWord = userPwd.getText().toString().trim();

            Intent intent = new Intent(this, XmppService.class);
            intent.putExtra("user", userName);
            intent.putExtra("pwd", passWord);
            //bindService(intent, mConnection, BIND_AUTO_CREATE);
            startService(intent);

            //mService.connectConnection(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickLogoutBtn(View view) {
        startService(new Intent(this, XmppService.class));
    }

    public void onClickSendBtn(View view) {
        try {
            ChatManager.getInstance().sendOneToOneMessage("t2");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }
}
