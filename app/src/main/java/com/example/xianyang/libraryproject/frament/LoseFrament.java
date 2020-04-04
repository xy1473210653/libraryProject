package com.example.xianyang.libraryproject.frament;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.xianyang.libraryproject.LoseAdapter;
import com.example.xianyang.libraryproject.MyLoader;
import com.example.xianyang.libraryproject.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class LoseFrament extends Fragment implements LoaderManager.LoaderCallbacks<List<Bitmap>> {
    private Thread thread;
    private ListView listView;
    private static final int GET_MESSGE=0x00;
    private static final int GET_PHOTO=0x01;
    private String[] list;
    private List<Bitmap> bitmapList;
    private boolean isok=false;
    private List<Map<String,Object>> datalist;
    private ProgressBar progressBar;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case GET_MESSGE:
                    Bundle bundle=msg.getData();
                    String loselist=bundle.getString("loselist");
                    parseMessage(loselist);
                    break;
                case GET_PHOTO:
                    bitmapList.add((Bitmap) msg.obj);
                    isok=true;
                    break;
                case 0x13:
                    Toast.makeText(getContext(),"请先登录",Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.lose_fragment,container,false);
        listView=view.findViewById(R.id.lose_listview);
        progressBar=view.findViewById(R.id.progress);
        recvdata();
        return view;
    }
    private void parseMessage(String loselist)
    {
         list=loselist.split("&");
         initdate();
         //handler.sendEmptyMessage(GET_PHOTO);
    }
    private void getPhoto(String id)
    {
        SharedPreferences sf=getActivity().getSharedPreferences("user",MODE_PRIVATE);
        if(sf.getBoolean("islogin",false)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket(getResources().getString(R.string.service_ip), 8080);
                        socket.setSoTimeout(100000);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("aim", "find_picture_article");
                        jsonObject.put("logid",id);
                        String result = jsonObject.toString();
                        OutputStream os = socket.getOutputStream();
                        os.write(result.getBytes());
                        os.flush();
                        socket.shutdownOutput();
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line = "";
                        String res = "";
                        while ((line = br.readLine()) != null) {
                            res += line;

                        }
                        Log.d("socketpoppppp", "run: "+res);
                        Message message=new Message();
                        message.what=GET_PHOTO;
                        byte[] array=Base64.decode(res,Base64.DEFAULT);
                        message.obj=BitmapFactory.decodeByteArray(array,0,array.length);
                        handler.sendMessage(message);
                        br.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
        else {
            handler.sendEmptyMessage(0x13);
        }
    }
    private void recvdata() {
        SharedPreferences sf=getActivity().getSharedPreferences("user",MODE_PRIVATE);
        if(sf.getBoolean("islogin",false)) {
            String finalId = sf.getString("id", null);
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket socket = null;
                    try {
                        socket = new Socket(getResources().getString(R.string.service_ip), 8080);
                        socket.setSoTimeout(10000);
                        OutputStream os = socket.getOutputStream();
                        JSONObject object = new JSONObject();
                        object.put("aim", "find_article");
                        object.put("id", finalId);
                        String result = object.toString();
                        os.write(result.getBytes());
                        os.flush();
                        socket.shutdownOutput();

                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line = "";
                        String res = "";
                        while ((line = br.readLine()) != null) {
                            res += line;
                        }
                        JSONObject jsonObject = new JSONObject(res);
                        String loselist = jsonObject.getString("loselog");
                        Log.d("socketpoppppp", "run: " + res);
                        Message message = new Message();
                        message.what = GET_MESSGE;
                        Bundle bundle = new Bundle();
                        bundle.putString("loselist",loselist );
                        message.setData(bundle);
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();
        }
        else {
            handler.sendEmptyMessage(0x13);
        }
    }

    private void initdate() {
        datalist=new ArrayList<Map<String, Object>>();
        for (int i=0;i<list.length;i++)
        {
            try {
                JSONObject object=new JSONObject(list[i]);
                Map<String,Object> map=new HashMap<>();
                map.put("isget","未领取");
                map.put("logid",object.getString("logid"));
                map.put("place",object.getString("caseid"));
                map.put("data",object.getString("time"));
                map.put("desc",object.getString("desc"));
                datalist.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        getLoaderManager().initLoader(1,null,this).forceLoad();
//        for (int i=0;i<list.length;i++)
//        {
//            JSONObject object= null;
//            try {
//                object = new JSONObject(list[i]);
//                getPhoto(object.getString("logid"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//        listView.setAdapter(new LoseAdapter(datalist,getContext(),bitmapList));
    }


    @NonNull
    @Override
    public Loader<List<Bitmap>> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new MyLoader(getContext(),list,"find_picture_article");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Bitmap>> loader, List<Bitmap> o) {
        progressBar.setVisibility(View.GONE);
        listView.setAdapter(new LoseAdapter(datalist,getContext(),o));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Bitmap>> loader) {
        new MyLoader(getContext(),list,"find_picture_article");
    }
}
