package com.example.xianyang.libraryproject.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.xianyang.libraryproject.FristActivity;
import com.example.xianyang.libraryproject.MainActivity;
import com.example.xianyang.libraryproject.R;
import com.example.xianyang.libraryproject.frament.MarketFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
//后台服务，与服务长连接，接收到服务器发送的消息，通知出去
public class MarketService extends Service {
    private Thread thread;
    private NotificationManager notifManager;//通知管理
    public static final String channel_id = "channel_1";
    public static final String name = "channel_name_1";
    public  Notification notification;
    public Handler handler=new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x11)
            {
                Log.d("socket", "handleMessage: "+msg.obj);
                try {
                    showNotification((String) msg.obj);//发送通知
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("socket", "onStartCommand: "+"start");
        //连接服务，随时接收数据
        thread=new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    Socket socket=new Socket("192.168.43.217",8080);
                    socket.setSoTimeout(10000);
                    while(true)
                    {

                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("aim","send_attention");
                        String result=jsonObject.toString();
                        OutputStream os=socket.getOutputStream();
                        os.write(result.getBytes());
                        os.flush();
                        //socket.shutdownOutput();
                        Log.d("aim", "run: "+result);
                            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String line="";
                            String res="";
                            while ((line=br.readLine())!=null)
                            {
                                res+=line;
                            }
                            if (res!=null)
                            {
                                JSONObject object=new JSONObject(res);
                                Message message=new Message();
                                message.what=0x11;
                                message.obj=res;
                                handler.sendMessage(message);
                            }

//                            String notification=object.getString("notification");
//                            Log.d("socket", "run: "+res);
                            br.close();
                        Thread.sleep(2000);
                        }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(String aMessage) throws JSONException {
        ///API27,通知写法。
        final int NOTIFY_ID = 0; // ID of notification
        String id = channel_id; // default_channel_id
        String title = name; // Default Channel
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, id);
            intent = new Intent(this, FristActivity.class);
            intent.putExtra("notification",aMessage);//传送接收到的消息
            //Log.d("market", "showNotification: "+aMessage);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle("图书馆通知")  // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(new JSONObject(aMessage).getString("content"))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(this.getString(R.string.app_name))
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {
            builder = new NotificationCompat.Builder(this);
            intent = new Intent(this, FristActivity.class);
            intent.putExtra("notification",aMessage);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle("图书馆通知")                           // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(new JSONObject(aMessage).getString("content"))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(this.getString(R.string.app_name))
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);
    }
}
