package com.xiifwat.xmppdemo;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Tawfiq on 03/10/2019
 */
public class ChatManager {

    public static final String RESOURCE = "Smack";
    private static final String DOMAIN = "vdo.sensor.buzz";
    private static final String HOST = "vdo.sensor.buzz";
//    private static final String HOST = "192.168.102.232";
    private static final int PORT = 5222;
    private static final int CONNECT_TIME_OUT = 8000;
    private String userName = "";
    private String passWord = "";
    AbstractXMPPConnection connection;
    org.jivesoftware.smack.chat2.ChatManager chatManager;
    ConnectionListener connectionListener = new XmppConnetionListener();
    MyIncomingChatMessageListener mIncomingChatMessageListener = new MyIncomingChatMessageListener();
    MyOutgoinMessageListener mOutgoinMessageListener = new MyOutgoinMessageListener();
    private boolean connected;
    private boolean isToasted = false;
    private boolean chat_created;
    private boolean loggedin;

    private static ChatManager instance;

    public static ChatManager getInstance() {

        if (instance == null) {
            synchronized (ChatManager.class) {
                if (instance == null) {
                    instance = new ChatManager();
                }
            }
        }

        return instance;
    }


    //Initialize
    public void init(String userId, String pwd) throws XmppStringprepException {
        if(connection!=null) return;

        Log.i("XMPP", "Initializing!");

        DomainBareJid serviceName = JidCreate.domainBareFrom(DOMAIN);

        this.userName = userId;
        this.passWord = pwd;

        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(userName, passWord);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setHostAddress(XmppUtil.getHostIp(HOST));
        configBuilder.setPort(PORT);
        configBuilder.setConnectTimeout(CONNECT_TIME_OUT);
        configBuilder.setCompressionEnabled(false);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setSendPresence(false);
        configBuilder.setXmppDomain(serviceName);
        configBuilder.setResource(RESOURCE);
        //configBuilder.setDebuggerEnabled(true);
        connection = new XMPPTCPConnection(configBuilder.build());
        connection.addConnectionListener(connectionListener);

        chatManager = org.jivesoftware.smack.chat2.ChatManager.getInstanceFor(connection);

        chatManager.addIncomingListener(mIncomingChatMessageListener);
        chatManager.addOutgoingListener(mOutgoinMessageListener);
    }

    // Disconnect Function
    public void disconnectConnection() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connectConnection() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... arg0) {

                try {
                    if(connection!=null && !connection.isConnected())
                        connection.connect();

                } catch (IOException | InterruptedException | SmackException | XMPPException e) {
                    Log.e("XMPP", "xmpp connect/login crashed", e);
                }

                return null;
            }
        };
        connectionThread.execute();
    }


    public void sendOneToOneMessage(String to) throws XmppStringprepException {

        if (connection==null || !connection.isConnected() || !connection.isAuthenticated()) {
            Log.e("xmpp","Not connected/authenticated. Message sending failed");
            return;
        }

        JivePropertiesExtension jpe = new JivePropertiesExtension();
        jpe.setProperty("timestamp", new Date().getTime());
        jpe.setProperty("resourcetype", 1);

        Message newMessage = new Message();
        newMessage.addExtension(jpe);
        newMessage.setBody("From demo app");
        newMessage.setType(Message.Type.chat);
        newMessage.setFrom(JidCreate.entityBareFrom(userName + "@" + DOMAIN));
        newMessage.setTo(JidCreate.entityBareFrom(to + "@" + DOMAIN));

        try {
            org.jivesoftware.smack.chat2.Chat newChat = chatManager.chatWith(
                    JidCreate.entityBareFrom(JidCreate.entityBareFrom(to + "@" + DOMAIN)));
            newChat.send(newMessage);

            Log.i("xmpp","OneToOne msd sent to " + to);
        } catch (XmppStringprepException | InterruptedException | SmackException.NotConnectedException e) {
            Log.e("xmpp","Send one to one message failed",e);
            e.printStackTrace();
        }
    }


    public void login() {

        try {
            if(connection!=null && connection.isConnected() && !connection.isAuthenticated())
                connection.login(userName, passWord);
            //Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");

        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

    }

    public void logout() {

        Thread thread = new Thread("xmpp_logout") {
            @Override
            public void run() {
                if (connection!=null && connection.isConnected()) {
                    Log.i("xmpp-logout", "connected now, disconnecting...");
                    connection.disconnect();
                    connection = null;
                } else {
                    Log.i("xmpp-logout", "already disconnected");
                }
            }

        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();

    }


    // Connection Listener to check connection state
    public class XmppConnetionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection connection) {
            Log.i("xmpp", "Connected!");
            connected = true;
            if (!connection.isAuthenticated()) {
                login();
            }
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            Log.i("xmpp", "Authenticated!");
            loggedin = true;

            chat_created = false;
        }

        @Override
        public void connectionClosed() {
            Log.i("xmpp", "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.i("xmpp", "ConnectionClosedOn Error!");
            connected = false;

            chat_created = false;
            loggedin = false;
        }
    }

    public class MyIncomingChatMessageListener implements IncomingChatMessageListener {

        @Override
        public void newIncomingMessage(EntityBareJid from, Message message, org.jivesoftware.smack.chat2.Chat chat) {
            Log.d("xmpp", String.format(
                    "Incoming msg '%s' from: %s", message.getBody(),
                    message.getFrom().asBareJid().getResourceOrEmpty().toString()
            ));
        }
    }

    public class MyOutgoinMessageListener implements OutgoingChatMessageListener {

        @Override
        public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
            Log.d("xmpp", String.format(
                    "Msg sent '%s' to: %s", message.getBody(),
                    message.getTo().asBareJid().getResourceOrEmpty().toString()
            ));
        }
    }

}
