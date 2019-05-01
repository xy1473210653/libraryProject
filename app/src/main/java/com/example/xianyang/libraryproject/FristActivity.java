package com.example.xianyang.libraryproject;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.xianyang.libraryproject.com.google.zxing.activity.CaptureActivity;
import com.example.xianyang.libraryproject.frament.BooksFragment;
import com.example.xianyang.libraryproject.frament.FindSeatFragment;
import com.example.xianyang.libraryproject.frament.LoseFrament;
import com.example.xianyang.libraryproject.frament.MarketFragment;
import com.example.xianyang.libraryproject.my.MyActivity;
import com.example.xianyang.libraryproject.service.MarketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主页面，一个滑动管理
 * fragment，实现，座位预约，图书，活动，失物调度
 */
public class FristActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private DrawerLayout drawerLayout;//滑动布局
    private Uri imageUri;//拍照图片路径
    private ImageViewPlus my_actionbar_head;//自定义actionbar图标
    private ImageButton my_camera;//自定义相机按钮
    private ImageButton my_sao;//扫一扫
    private ActionBarDrawerToggle drawerToggle;//actionbar管理，实现在actionbar点击弹出抽屉
    private ViewPager viewPager;
    private List<Fragment> mTabs=new ArrayList<Fragment>();//装载碎片list
    private FragmentPagerAdapter fragmentPagerAdapter;
    private List<ChangeColorIconWithText> mTabIndicators=new ArrayList<ChangeColorIconWithText>();//装载自定义图标对象
    private Thread thread;
    private  File outputImagepath;//拍照图片文件
    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    private boolean isFirst=true;//是否第一次刷新页面
    private ImageViewPlus imageViewPlus;//侧滑头像
    //扫描成功返回码
    //private int RESULT_CODE_QR_SCAN = 0xA1;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x00)
            {
                Log.d("handle111111", "handleMessage: ");
                my_actionbar_head.setImageBitmap((Bitmap) msg.obj);
                imageViewPlus.setImageBitmap((Bitmap) msg.obj);
            }
            else if(msg.what==0x11)
            {
                Toast.makeText(FristActivity.this, "请先登录", Toast.LENGTH_SHORT).show();

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.frist_activity);
       Log.d("market", "onCreateView: "+getIntent().getStringExtra("notification"));
        drawerLayout=findViewById(R.id.drawerLayout);
       initView();//初始化viewPager，初始化工具栏图标按钮
        initDatas();//初始化碎片，配置碎片适配器
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOnPageChangeListener(this);
        getHeadImage();
        initActionBar();
        initLeftDrawerLaytou();
       //让drawerlayout与actionbar关联
       drawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerToggle.syncState();
        drawerLayout.setDrawerListener(drawerToggle);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        Intent intent=new Intent(this,MarketService.class);
        startService(intent);

    }

    private void getHeadImage() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sf = getSharedPreferences("user", MODE_PRIVATE);
                if (sf.getBoolean("islogin", false)) {
                    String finalId = sf.getString("id", null);
                    String passwd=sf.getString("passwd",null);
                    try {
                        Socket socket = new Socket("192.168.43.217", 8080);
                        socket.setSoTimeout(10000);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("aim", "get_photo_picture");
                        jsonObject.put("id", finalId);
                        jsonObject.put("password", passwd);
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
                        //JSONObject object = new JSONObject(res);
                        //String result1 = object.getString("result");
                        Message message = new Message();
                        message.what = 0x00;
                        byte[] array = Base64.decode(res, Base64.DEFAULT);
                        message.obj = BitmapFactory.decodeByteArray(array, 0, array.length);
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
                } else {
                    handler.sendEmptyMessage(0x11);
                }
            }
        });
        thread.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {

                }
                break;
        }
        isFirst=true;
    }
    //初始化侧滑，配置侧滑按钮点击事件
    private void initLeftDrawerLaytou() {

        ListView listView;
        int[] res={R.mipmap.data_jie,R.mipmap.data,R.mipmap.lost,R.mipmap.er_wei_ma,R.mipmap.setting,R.mipmap.quit};
        String[] text={"选座记录","借阅记录","失物认领记录","我的二维码","设置","注销登录"};
        List<Map<String,Object>> datalist=new ArrayList<Map<String, Object>>();
        SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);

        imageViewPlus=findViewById(R.id.image_head);
        imageViewPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sf.getBoolean("islogin",false))
                {
                    startActivity(new Intent(FristActivity.this,MyActivity.class));
                }
                else {
                    startActivity(new Intent(FristActivity.this,MainActivity.class));
                }
            }
        });
        listView=findViewById(R.id.left_listview);
        for (int i=0;i<6;i++)
        {
            Map<String,Object> map=new HashMap<>();
            map.put("image",res[i]);
            map.put("text",text[i]);
            datalist.add(map);
        }
        listView.setAdapter(new DrawerLeftAdapter(this,datalist));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 5) {
                    SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
                    if (sf.getBoolean("islogin",false))
                    {
                        //未登陆时，点击注销登陆，跳转登陆页面
                        startActivity(new Intent(FristActivity.this, MainActivity.class));
                    }else {

                    }
                }
            }
        });

    }
    private String getBase64Image(Bitmap bitmap)
    {
        String result=null;
        //mbitmap=BitmapFactory.decodeResource(this.getResources(),id);//根据图片路劲得到bitmap
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,40,baos);
        byte[] data=baos.toByteArray();
        result= Base64.encodeToString(data,Base64.DEFAULT);//编码
        return result;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Log.e("image", "onActivityResult: " + "读取失败");
        } else {
            //调用扫描二维码回调

            if (requestCode==REQUEST_CODE)
            {
                SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
                String id="";
                Bundle bundle=data.getExtras();
                if(sf.getBoolean("islogin",false)) {
                    id = sf.getString("id", null);
                    String finalId = id;
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket socket = new Socket("192.168.43.217", 8080);
                                socket.setSoTimeout(10000);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("aim", "get_seat");
                                jsonObject.put("id", finalId);
                                jsonObject.put("seatid", bundle.getString("qr_scan_result"));
                                String result = jsonObject.toString();
                                Log.d("socket", "run: " + bundle.getString("qr_scan_result"));
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
                                JSONObject object = new JSONObject(res);
                                String result1 = object.getString("result");
                                if (Boolean.parseBoolean(result1)) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(FristActivity.this, "入座成功", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(FristActivity.this, "入座失败", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                Log.d("socket", "run: " + result1);

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
                }else {
                    handler.sendEmptyMessage(0x11);
                }
            }
            //调用相机拍照回调
            if (requestCode==200)
            {
                //Bundle bundle=data.getExtras();
               // Bitmap mbitmap= (Bitmap) bundle.get("data");

                SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
                String id="";
                if(sf.getBoolean("islogin",false))
                {
                    id=sf.getString("id",null);
                    String finalId = id;
                    thread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket socket=new Socket("192.168.43.217",8080);
                                socket.setSoTimeout(10000);
                                //ByteArrayOutputStream bout=new ByteArrayOutputStream();
                                //mbitmap.compress(Bitmap.CompressFormat.JPEG,100,bout);
                                //long length =bout.size();
                                long length=outputImagepath.length();
                                JSONObject jsonObject=new JSONObject();
                                jsonObject.put("aim","send_picture");
                                jsonObject.put("id", finalId);
                                jsonObject.put("filesize",length);
                                String result=jsonObject.toString();
                                Log.d("socket", "run: "+length);
                                OutputStream os=socket.getOutputStream();
                                os.write(result.getBytes());
                                os.flush();
                                //上传图片至服务器
                                FileInputStream fileInputStream=new FileInputStream(outputImagepath.getAbsoluteFile());
                                int size=-1;
                                byte[] buffer =new byte[1024];
                                while( (size=fileInputStream.read(buffer,0,1024))!=-1)
                                {
                                    os.write(buffer,0,size);
                                }
                                fileInputStream.close();
                                os.close();
                                socket.shutdownOutput();//自动断开连接
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

                }else {
                   handler.sendEmptyMessage(0x11);
                }


            }
        }
    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId())
//        {
//            case android.R.id.home:
//                drawerToggle.onOptionsItemSelected(item);
//                break;
//            case R.id.camera:
//                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////              Uri uri=Uri.fromFile(new File(path));
////              intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
//                startActivityForResult(intent,200);
//                break;
//            case R.id.sao:
//                Intent intent1 = new Intent(FristActivity.this, CaptureActivity.class);
//                startActivityForResult(intent1, REQUEST_CODE);
//                break;
//            default:
//                    break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
    //隐藏actionbar，初始化自定义类actionbar
    private void initActionBar() {
        //获得actionbarguanli
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        my_actionbar_head=findViewById(R.id.my_actionbar_head);
        my_camera=findViewById(R.id.my_actionbar_camera);
        my_sao=findViewById(R.id.my_actionbar_sao);
        //点击事件弹出和隐藏抽屉
        my_actionbar_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT))
                {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });
       //拍照上传点击事件
        my_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //根据时间定义图片名字
                SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                        "yyyy_MM_dd_HH_mm_ss");
                String filename = timeStampFormat.format(new Date());
                //创建图片文件对象
                outputImagepath = new File(Environment.getExternalStorageDirectory(),
                        filename + ".jpg");
                //保存图片
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, outputImagepath.getAbsolutePath());
                Uri uri = getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                startActivityForResult(intent,200);
            }
        });
        //扫一扫点击事件
        my_sao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(FristActivity.this, CaptureActivity.class);
                startActivityForResult(intent1, REQUEST_CODE);
            }
        });
    }

    /**
     * 初始化fragment，添加碎片
     */
    private void initDatas() {
        FindSeatFragment findSeatFragment=new FindSeatFragment();
        BooksFragment booksFragment=new BooksFragment();
        MarketFragment marketFragment=new MarketFragment();
        LoseFrament loseFrament=new LoseFrament();
        mTabs.add(findSeatFragment);
        mTabs.add(booksFragment);
        mTabs.add(marketFragment);
        mTabs.add(loseFrament);
        fragmentPagerAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return mTabs.get(i);
            }

            @Override
            public int getCount() {
                return mTabs.size();
            }
        };
    }

    /**
     * 初始化自定义图标
     */
    private void initView() {
        viewPager=findViewById(R.id.viewPager);
        ChangeColorIconWithText findSeat=findViewById(R.id.find_seat_item);
        mTabIndicators.add(findSeat);
        ChangeColorIconWithText books=findViewById(R.id.books_item);
        mTabIndicators.add(books);
        ChangeColorIconWithText market=findViewById(R.id.market_item);
        mTabIndicators.add(market);
        ChangeColorIconWithText lose=findViewById(R.id.lose_item);
        mTabIndicators.add(lose);
        findSeat.setOnClickListener(this);
        books.setOnClickListener(this);
        market.setOnClickListener(this);
        lose.setOnClickListener(this);
        findSeat.setIconAlpha(1.0f);
    }
   //图标点击事件
    @Override
    public void onClick(View v) {
        resetOtherTabs();
        switch (v.getId())
        {
            case R.id.find_seat_item:
                mTabIndicators.get(0).setIconAlpha(1);
                viewPager.setCurrentItem(0,false);
                break;
            case R.id.books_item:
                mTabIndicators.get(1).setIconAlpha(1);
                viewPager.setCurrentItem(1,false);
                break;
            case R.id.market_item:
                mTabIndicators.get(2).setIconAlpha(1);
                viewPager.setCurrentItem(2,false);
                break;
            case R.id.lose_item:
                mTabIndicators.get(3).setIconAlpha(1);
                viewPager.setCurrentItem(3,false);
                break;
        }
    }
    //将所有图标透明度设置为0
    private void resetOtherTabs() {
        for (int i=0;i<mTabIndicators.size();i++)
        {
            mTabIndicators.get(i).setIconAlpha(0);
        }
    }
    //viewpager滑动，图标跟随切换
    @Override
    public void onPageScrolled(int i, float v, int i1) {
        if (v>0) {
            ChangeColorIconWithText left = mTabIndicators.get(i);
            ChangeColorIconWithText right = mTabIndicators.get(i + 1);
            left.setIconAlpha(1 - v);//透明度取反
            right.setIconAlpha(v);
        }
    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera,menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu!=null)
        {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod
                            ("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }
    //将actionbal三个点更改图标
    private void setOverflowButtonAlways()
    {

        try {
            ViewConfiguration configuration=ViewConfiguration.get(this);
            Field menuKey=ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKey.setAccessible(true);
            menuKey.setBoolean(configuration,false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    public static void show(Context context,Intent intent){
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d("ssssss", "onResume: "+isFirst);
//        if (!isFirst)
//        {
//            //Intent intent=getIntent();
//            resetOtherTabs();
//            mTabIndicators.get(2).setIconAlpha(1);
//            viewPager.setCurrentItem(2,false);
//            isFirst=false;
//        }

    }
}
