package com.example.testdrawer;

import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.testdrawer.bean.Photo;
import com.example.testdrawer.utils.GsonUtils;
import com.example.testdrawer.utils.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * MainActivity
 * 加载侧滑菜单，以及初始化服务器图片
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public static List<Photo> photoList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        initPhotos();
    }
    /**
     * 拉取服务器的图片到本地来
     * @exception IOException On input error.
     */
    private void initPhotos() {
        new Thread() {
            public void run() {
                String url = "http://47.94.254.10:3000/img/photos";
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    JSONArray listObject = JSONArray.parseArray(response.body().string());
                    JSONObject jb = new JSONObject();
                    for(int i=0;i<listObject.size();i++) {
                        Photo photo = new Photo();
                        jb = listObject.getJSONObject(i);
                        photo.set__v((int)jb.get("__v"));
                        photo.set_id((String)jb.get("_id"));
                        photo.setContent((String)jb.get("content"));
                        photo.setCreateDate((String) jb.get("createDate"));
                        photo.setName((String)jb.get("name"));
                        photo.setSize((int)jb.get("size"));
                        String detect_url = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
                        try {
                            Map<String, Object> map = new HashMap<>();

                            map.put("image", photo.getContent());
                            map.put("max_face_num",10);
                            map.put("face_field", "faceshape,facetype");
                            map.put("image_type", "BASE64");
                            String param = GsonUtils.toJson(map);
                            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                            String accessToken = "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208";
                            String result = HttpUtil.post(detect_url, accessToken, "application/json", param);
                            photo.setDetect_info(result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        photoList.add(photo);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
