package com.example.testdrawer.ui.send;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.example.testdrawer.R;
import com.example.testdrawer.utils.Base64Util;
import com.example.testdrawer.utils.FileUtil;
import com.example.testdrawer.utils.GsonUtils;
import com.example.testdrawer.utils.HttpUtil;
import com.example.testdrawer.utils.ImageUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 人脸对比页面
 */
public class SendFragment extends Fragment {

    private SendViewModel sendViewModel;
    private ImageView img1;
    private ImageView img2;
    private TextView score;
    private Button button;

    private String uploadFileName1;
    private String uploadFileName2;
    private byte[] fileBuf1;
    private byte[] fileBuf2;

    private int click_button=1;

    private String uploadUrl = "http://47.94.254.10:3000/img/upload";
    private static String access_token_detect = "24.6ac6cac20c6ddd02effa33e9d53d10d6.2592000.1575943062.282335-17737224";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sendViewModel =
                ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        img1 = root.findViewById(R.id.img1);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click_button = 1;
                select(v);
            }
        });

        img2 = root.findViewById(R.id.img2);
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click_button = 2;
                select(v);
            }
        });
        score = root.findViewById(R.id.score);

        button = root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                match(v);
            }
        });

        final TextView textView = root.findViewById(R.id.text_send);
        sendViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    /**
     * 实现人脸对比功能
     * @param view
     */
    public void match(View view){
        //创建handler
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0x11) {
                    //更新ui
                    Bundle bundle = msg.getData();
                    String data = bundle.getString("score");
                    score.setText(data);
                }
            }
        };
        final Message message=handler.obtainMessage();
        new Thread(){
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
                try {
                    Map<String, Object> map = new HashMap<>();
                    String imgStr = Base64Util.encode(fileBuf1);
                    map.put("image", imgStr);
                    map.put("image_type", "BASE64");
                    Map<String, Object> map1 = new HashMap<>();
                    String imgStr1 = Base64Util.encode(fileBuf2);
                    map1.put("image",imgStr1);
                    map1.put("image_type", "BASE64");
                    String param = "["+ GsonUtils.toJson(map)+","+GsonUtils.toJson(map1)+"]";
                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                    String accessToken = "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208";
                    String result = HttpUtil.post(url, accessToken, "application/json", param);
                    JSONObject return_result = new JSONObject(result).getJSONObject("result");
                    String score = return_result.getString("score");
                    Bundle bundle = new Bundle();
                    bundle.putString("score", score);
                    message.what = 0x11;
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    /**
     *  按钮点击事件,选择照片
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
            if (click_button ==1)
                uploadFileName1 = cursor.getString(columnIndex);
            else
                uploadFileName2 = cursor.getString(columnIndex);
        }
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            if (click_button ==1){
                fileBuf1=convertToBytes(inputStream);
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf1, 0, fileBuf1.length);
                img1.setImageBitmap(bitmap);
            }else{
                fileBuf2=convertToBytes(inputStream);
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf2, 0, fileBuf2.length);
                img2.setImageBitmap(bitmap);
            }
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