package com.example.xianyang.libraryproject.frament;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.xianyang.libraryproject.FristActivity;
import com.example.xianyang.libraryproject.R;
import com.example.xianyang.libraryproject.socket.SeatMessage;
import com.qfdqc.views.seattable.SeatTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FindSeatFragment extends Fragment {
     SeatTable seatTable;
     private Thread thread;
     private Spinner floorSpinner;//楼层列表
     private Spinner areaSpinner;//区域列表
     private static final int CHANG_SCREEN_NAME=0x01;
     String floorText;//楼层
     String areaText;//区域
    String textF;
    String textA;
    SeatMessage seatMessage;

     Handler handler=new Handler(){
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             switch (msg.what)
             {
                 case CHANG_SCREEN_NAME:
                     Bundle bundle=msg.getData();
                     String[] seatno=bundle.getStringArray("seatno");
                     Log.d("socket", "handleMessage: "+seatno[0]);
                     seatChange(seatno);
                     break;
                 case 0x00:
                     recv();
                     break;
             }
         }
     };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.find_seat_fragment,container,false);
        floorSpinner=view.findViewById(R.id.floor_spinner);
        areaSpinner=view.findViewById(R.id.area_spinner);
        seatTable=view.findViewById(R.id.seatView);
        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textF= getResources().getStringArray(R.array.floor_spinner)[position];
                floorText=String.valueOf((position+1));
                Log.d("socket", "onItemSelected: "+textF);
                handler.sendEmptyMessage(0x00);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textA=getResources().getStringArray(R.array.area_spinner)[position];
                areaText=textA;
                handler.sendEmptyMessage(0x00);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    /**
     * 获取对应座位表信息
     */
    private void recv() {
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("socket", "run: "+floorText+areaText);
                    Socket socket=new Socket(getResources().getString(R.string.service_ip),8080);
                    socket.setSoTimeout(10000);
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("aim","seat_find");
                    jsonObject.put("floor",floorText);
                    jsonObject.put("area",areaText);
                    String result=jsonObject.toString();
                    OutputStream os=socket.getOutputStream();
                    os.write(result.getBytes());
                    os.flush();
                    socket.shutdownOutput();

                    BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line="";
                    String res="";
                    while ((line=br.readLine())!=null)
                    {
                        res+=line;
                    }
                    JSONObject object=new JSONObject(res);
                   // Log.d("socket", "runseatno: "+object.getString("author"));
                    String[] seatno=object.getString("seatno").split(",");
                    Message message=new Message();
                    message.what=CHANG_SCREEN_NAME;
                    Bundle bundle=new Bundle();
                    bundle.putStringArray("seatno",seatno);
                    message.setData(bundle);
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
        });
        thread.start();
    }

    /**
     * 发送预约请求
     * @param seatID
     */
    public void sendSeatMessage(int seatID)
    {
        SharedPreferences sf=getActivity().getSharedPreferences("user",MODE_PRIVATE);
        String userID=sf.getString("id",null);
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("socket", "run: "+seatID);
                    Socket socket=new Socket(getResources().getString(R.string.service_ip),8080);
                    socket.setSoTimeout(10000);
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("aim","seat_reserve");
                    jsonObject.put("floor",floorText);
                    jsonObject.put("area",areaText);
                    jsonObject.put("id",userID);
                    jsonObject.put("seatno",String.valueOf(seatID));
                    String result=jsonObject.toString();
                    OutputStream os=socket.getOutputStream();
                    os.write(result.getBytes());
                    os.flush();
                    socket.shutdownOutput();
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
        });
        thread.start();
    }
    public void seatChange(String[] seatno)
    {
        Log.d("ScreenName", "seatChange: "+textF+textA);
        seatTable.setScreenName(textF+textA+"区");
        seatTable.setMaxSelected(1);//最大选择
        seatTable.setSeatChecker(new SeatTable.SeatChecker() {
            @Override
            public boolean isValidSeat(int row, int column) {//是否是无效座位

                return true;
            }

            @Override
            public boolean isSold(int row, int column) {//是否已经出售
              List<String> list= Arrays.asList(seatno);
                if (list.contains(String.valueOf((row)*10+column+1)))
                {
                    Log.d("socket", "isSold: "+row+"ww:"+column+"  "+((row+1)*10+column+1));
                    return true;
                }
                return false;
            }

            @Override
            public void checked(int row, int column) {//是否已经选择
                Log.d("TAG", "checked: "+row+"   "+column);

                AlertDialog dialog=new AlertDialog.Builder(getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("选择座位")
                        .setMessage("你选择"+(row+1)+"排"+(column+1)+"座")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendSeatMessage((row)*10+column+1);
                                Toast.makeText(getContext(),"你选择"+(row+1)+"排"+(column+1)+"座",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.sendEmptyMessage(0x00);
                            }
                        }).show();

            }

            @Override
            public void unCheck(int row, int column) {

            }

            @Override
            public String[] checkedSeatTxt(int row, int column) {
                return new String[0];
            }
        });
        seatTable.setData(8,5);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
