package com.example.testdrawer.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.testdrawer.MainActivity;
import com.example.testdrawer.R;
import com.example.testdrawer.bean.Photo;
import com.example.testdrawer.utils.GsonUtils;
import com.example.testdrawer.utils.HttpUtil;
import com.example.testdrawer.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 图片上传页面
 */
public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    // 上传图片所需参数
    private ImageView photo;
    private Button upload;
    private Button select;

    private String uploadFileName;
    private byte[] fileBuf;
    private String uploadUrl = "http://47.94.254.10:3000/img/upload";
    private static String access_token_detect = "24.6ac6cac20c6ddd02effa33e9d53d10d6.2592000.1575943062.282335-17737224";
   // public static List<Photo> photoList = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        photo = root.findViewById(R.id.photo);
        upload = root.findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload(v);
            }
        });
        select = root.findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(v);
            }
        });
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    /**
     * 文件上传的处理
     * @param view
     */
    public void upload(View view) {
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                //上传文件域的请求体部分
                RequestBody formBody = RequestBody
                        .create(fileBuf, MediaType.parse("image/jpeg"));
                //整个上传的请求体部分（普通表单+文件上传域）
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", "Square Logo")
                        //filename:avatar,originname:abc.jpg
                        .addFormDataPart("avatar", uploadFileName, formBody)
                        .build();
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    //把上传的照片信息存到photoList中
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
                    Photo photo = new Photo();
                    photo.setContent(ImageUtils.bitmapToBase64(bitmap));
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
                    MainActivity.photoList.add(photo);
                    //上传文件返回结果
                    Looper.prepare();
                    Toast.makeText(getActivity(),response.body().string(),Toast.LENGTH_LONG).show();
                    Looper.loop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 按钮点击事件，选择照片
     * @param view
     */
    public void select(View view) {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        //进行sdcard的读写请求
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        } else {
            openGallery(); //打开相册，进行选择
        }
    }
    /**
     *  请求读取相册权限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(getActivity(), "读相册的操作被拒绝", Toast.LENGTH_LONG).show();
                }
        }
    }
    /**
     * 打开相册,进行照片的选择
     */
    private void openGallery() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                handleSelect(data);
        }
    }
    /**
     * 选择后照片的读取工作
     * @param intent
     */
    private void handleSelect(Intent intent) {
        Cursor cursor = null;
        Uri uri = intent.getData();
        cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            uploadFileName = cursor.getString(columnIndex);
        }
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            fileBuf=convertToBytes(inputStream);
            Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
            photo.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
    }
    /**
     * 把inputStream转换为字节数组
     * @param inputStream
     * @return
     * @throws Exception
     */
    private byte[] convertToBytes(InputStream inputStream) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
        return  out.toByteArray();
    }



}