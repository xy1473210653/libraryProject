package com.example.xianyang.libraryproject.books;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

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

/**
 * 查找书活动，列出对应图书列表
 *    点击图书，显示图书详细信息的对话框
 */
public class Lend_book extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Bitmap>>  {
    private List<Map<String,Object>> datalist;
    private  BookAdapter bookAdapter;
    private ListView listView;
    private Thread thread;
    private Thread book_thread;
    private Spinner spinner;
    private String book_dire;
    private String[] books;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x00)
            {
                Bundle bundle=msg.getData();
                String booklist=bundle.getString("booklist");
                initData(booklist);
            }
            if (msg.what==0x01)
            {
                Bundle bundle=msg.getData();
                Intent intent=new Intent(Lend_book.this,ShowBook.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_books);
        listView=findViewById(R.id.book_list);
        spinner=findViewById(R.id.books_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 book_dire=getResources().getStringArray(R.array.book_spinner)[0];
                Log.d("spinner", "onItemSelected: "+book_dire);
                 getData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    /**
     * 获取图书数据后更新listview
     * @param booklist
     */
    private void initData(String booklist)
    {
        Log.d("books", "initData: "+booklist);
        books=booklist.split("&");
        datalist=new ArrayList<Map<String, Object>>();
        Map<String,Object> map;
        for (int i=0;i<books.length;i++)
        {
            Log.d("booklist", "initData: "+books[i]);
            map=new HashMap<String, Object>();
            try {
                JSONObject jsonObject=new JSONObject(books[i]);
                //map.put("image",R.drawable.ic_launcher_foreground);
                map.put("name",jsonObject.getString("bookname"));
                map.put("writer",jsonObject.getString("author"));
                map.put("brief",jsonObject.getString("introduct"));
                datalist.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getSupportLoaderManager().initLoader(2,null,this).forceLoad();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message=new Message();
                Bundle bundle=new Bundle();
                bundle.putString("book",books[position]);
                message.what=0x01;
                message.setData(bundle);
                handler.sendMessage(message);
//                Intent intent=new Intent(Lend_book.this,ShowBook.class);
//                intent.putExtras(bundle);
//                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(Lend_book.this)
                        .setTitle("你是否要预约该书？")
                        .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
                                        if(sf.getBoolean("islogin",false)) {
                                            String finalId = sf.getString("id", null);
                                            Socket socket = null;
                                            try {
                                                socket = new Socket("192.168.43.217", 8080);
                                                socket.setSoTimeout(10000);
                                                OutputStream os = socket.getOutputStream();
                                                JSONObject object = new JSONObject();
                                                object.put("aim", "book_reserve");
                                                object.put("logid", finalId);
                                                object.put("bookname",new JSONObject(books[position]).getString("bookname"));
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
                                                if (Boolean.parseBoolean(new JSONObject(res).getString("result")))
                                                {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(Lend_book.this,"预约成功",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                else {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(Lend_book.this,"预约失败",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }).start();
                            }
                        })
                        .setPositiveButton("取消",null)
                        .show();
                return true;
            }
        });
    }

    /**
     * 发送socket请求，请求对应的图书类列表
     */
    private void getData() {
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket=new Socket("192.168.43.217",8080);
                    socket.setSoTimeout(10000);
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("aim","find_book_direction");
                   // jsonObject.put("class",book_dire);
                    jsonObject.put("class",book_dire);
                    //jsonObject.put("id",userID);
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
                    String booklist=object.getString("book");
                    Log.d("socket", "run: "+booklist);
                    Message message=new Message();
                    message.what=0x00;
                    Bundle bundle=new Bundle();
                    bundle.putString("booklist",booklist);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.actionbar_seachview,menu);
        MenuItem item = menu.findItem(R.id.action_seach);
        SearchView searchView= (SearchView) item.getActionView();
        searchView.setIconified(false);
        searchView.setQueryHint("请输入书名");
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                seachBook(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    public void seachBook(String bookname)
    {
        book_thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket=new Socket("192.168.43.217",8080);
                    socket.setSoTimeout(10000);
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("aim","find_book_single");
                    jsonObject.put("bookname",bookname);
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
                    String book_return=object.getString("result");
                    if (Boolean.parseBoolean(book_return))
                    {
                        Log.d("socket", "run: "+book_return);
                        Message message=new Message();
                        message.what=0x01;
                        Bundle bundle=new Bundle();
                        bundle.putString("book",res);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
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
        book_thread.start();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<List<Bitmap>> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new MyLoader(Lend_book.this,books,"find_book_picture");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Bitmap>> loader, List<Bitmap> bitmapList) {
        bookAdapter=new BookAdapter(Lend_book.this,datalist,bitmapList);
        listView.setAdapter(bookAdapter);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Bitmap>> loader) {
        new MyLoader(Lend_book.this,books,"find_book_picture");
    }
}
