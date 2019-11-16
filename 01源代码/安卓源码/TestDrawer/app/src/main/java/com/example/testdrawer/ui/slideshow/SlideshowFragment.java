package com.example.testdrawer.ui.slideshow;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.testdrawer.utils.GsonUtils;
import com.example.testdrawer.utils.HttpUtil;
import com.example.testdrawer.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 人脸注册页面
 */
public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;
    private ImageView photo;
    private EditText user_id_info;
    private Button select_photo;
    private Button insert_face;
    private String uploadFileName;
    private byte[] fileBuf;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        //初始化
        photo = root.findViewById(R.id.face_img);
        user_id_info = root.findViewById(R.id.user_id);
        select_photo = root.findViewById(R.id.select_photo);
        select_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFace(v);
            }
        });
        insert_face = root.findViewById(R.id.insert_face);
        insert_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insert_face(v);
            }
        });
        final TextView textView = root.findViewById(R.id.text_slideshow);
        slideshowViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    /**
     * 按钮点击事件     选择照片
     * @param view
     */
    public void selectFace(View view) {
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
     * 按钮点击事件    添加人脸
     * @param view
     */
    public void insert_face(View view) {
        new Thread() {
            @Override
            public void run() {
                // 请求url
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
                try {
                    Map<String, Object> map = new HashMap<>();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
                    map.put("image", ImageUtils.bitmapToBase64(bitmap));
                    map.put("image_type", "BASE64");
                    map.put("group_id", "face_group");
                    map.put("user_id",ImageUtils.getUUID());
                    map.put("user_info", user_id_info.getText().toString());
                    String param = GsonUtils.toJson(map);
                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                    String accessToken = "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208";
                    String result = HttpUtil.post(url, accessToken, "application/json", param);
                    Looper.prepare();
                    Toast.makeText(getActivity(),"添加成功",Toast.LENGTH_LONG).show();
                    Looper.loop();
                    //Log.d("添加人脸库信息",result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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