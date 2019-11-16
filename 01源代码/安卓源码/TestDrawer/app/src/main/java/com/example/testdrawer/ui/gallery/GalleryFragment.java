package com.example.testdrawer.ui.gallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
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

import com.example.testdrawer.R;
import com.example.testdrawer.utils.GsonUtils;
import com.example.testdrawer.utils.HttpUtil;
import com.example.testdrawer.utils.ImageUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
/**
 *  人脸搜索页面
 */
public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    private ImageView photo;
    private Button search_button;
    private Button select_button;
    private byte[] fileBuf;
    private String uploadFileName;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        photo = root.findViewById(R.id.imageOfSearch);
        search_button = root.findViewById(R.id.search_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchImageInfo(v);
            }
        });
        select_button = root.findViewById(R.id.select_button);
        select_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSearchImage(v);
            }
        });
        final TextView textView = root.findViewById(R.id.text_gallery);
        galleryViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
    /**
     * 按钮点击事件，搜索照片
     */
    public void searchImageInfo(View view) {
        new Thread() {
            @Override
            public void run() {
                // 请求url
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/multi-search";
                try {
                    Map<String, Object> map = new HashMap<>();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
                    map.put("image", ImageUtils.bitmapToBase64(bitmap));
                    map.put("image_type", "BASE64");
                    map.put("group_id_list", "face_group");
                    map.put("max_face_num", 10);
                    String param = GsonUtils.toJson(map);
                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                    String accessToken = "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208";
                    String result = HttpUtil.post(url, accessToken, "application/json", param);
                    //Log.d("人脸搜索结果",result);
                    showResult(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 显示人脸搜索的结果
     * @param result
     */
    public void showResult(String result){
        try {
            //先获取当前照片
            Bitmap current_bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
            //定义画笔
            Paint paint = new Paint();//画笔
            paint.setColor(Color.RED);//设置颜色
            //获取当前图像的长宽
            int w = current_bitmap.getWidth();
            int h = current_bitmap.getHeight();
            //创建空白图像
            Bitmap new_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);//创建一个新的和SRC长度宽度一样的位图
            Canvas cv = new Canvas(new_bitmap);
            //画原图
            cv.drawBitmap(current_bitmap, 0, 0, null);//在 0，0坐标开始画入src
            //解析搜索结果
            String user_name = "";
            try {
                JSONObject return_result = new JSONObject(result).getJSONObject("result");
                JSONArray face_list = return_result.getJSONArray("face_list");
                int face_num = return_result.getInt("face_num");
                for(int i = 0 ; i < face_num ; i++){
                    JSONObject user_face = face_list.getJSONObject(i);
                    // 获取用户信息及脸部位置
                    JSONObject face_location = user_face.getJSONObject("location");
                    int left = face_location.getInt("left");
                    int top = face_location.getInt("top");
                    int width = face_location.getInt("width");
                    int height = face_location.getInt("height");
                    Rect rect = new Rect(left ,top ,left + width,top+height);
                    paint.setStrokeWidth(5);
                    paint.setStyle(Paint.Style.STROKE);
                    cv.drawRect(rect,paint);
                    JSONArray user_list = user_face.getJSONArray("user_list");
                    if (user_list.length() > 0){
                        JSONObject user_info = user_list.getJSONObject(0);
                        user_name = user_info.getString("user_info");
                    }else {
                        user_name="人脸库中不存在";
                    }
                    paint.setTextSize(w/40);
                    paint.setStyle(Paint.Style.FILL);
                    cv.drawText(user_name, left,top-10, paint);
                }
            }catch (Exception e){
                e.printStackTrace();
                paint.setTextSize(40);
                paint.setStyle(Paint.Style.FILL);
                cv.drawText("未找到结果", 50,50, paint);
            }
            photo.setImageBitmap(new_bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //按钮点击事件     选择照片
    /**
     *  按钮点击事件,选择照片
     * @param view
     */
    public void selectSearchImage(View view) {
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

    //打开相册,进行照片的选择

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

    //选择后照片的读取工作

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