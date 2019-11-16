package com.example.testdrawer.ui.share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.example.testdrawer.MainActivity;
import com.example.testdrawer.R;
import com.example.testdrawer.bean.Photo;
import com.example.testdrawer.utils.ImageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 人脸检测页面
 */
public class ShareFragment extends Fragment {

    private GridView gridview;
    public static List<Rect> face_list = new ArrayList<>();;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_share, container, false);
        gridview = root.findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(getContext()));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.i("gridview", "这是第" + position + "幅图像。");
                Photo photo = MainActivity.photoList.get(position);
                String photo_result = photo.getDetect_info();
                org.json.JSONObject obj = null;
                try {
                    obj = new org.json.JSONObject(photo_result);
                    face_list.clear();
                    org.json.JSONObject ob = (org.json.JSONObject)obj.get("result");
                    int face_num = ob.getInt("face_num");
                    JSONArray face_array = ob.getJSONArray("face_list");
                    for (int i = 0 ; i < face_num ; i++){
                        JSONObject face_location_obj = face_array.getJSONObject(i).getJSONObject("location");
                        int left = face_location_obj.getInt("left");
                        int top = face_location_obj.getInt("top");
                        int width = face_location_obj.getInt("width");
                        int height = face_location_obj.getInt("height");
                        Log.d("测试长度",photo.getName()+left+":"+top+":"+width+":"+height);
                        Rect rect = new Rect(left,top,left + width,top+height);
                        face_list.add(rect);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getActivity(),LargeImgActivity.class);
                Bundle myBundle = new Bundle();
                myBundle.putInt("position",position);
                intent.putExtras(myBundle);
                getActivity().startActivity(intent);
            }
        });
        return root;
    }

    /**
     *  GridView数据适配器类
     */
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        public ImageAdapter(Context c) {
            mContext = c;
        }
        /**
         * 获取当前图片数量
         */
        @Override
        public int getCount() {
            return MainActivity.photoList.size();
        }
        /**
        根据需要position获得在GridView中的对象
         */
        @Override
        public Object getItem(int position) {
            return position;
        }
        /**获得在GridView中对象的ID
        */
        @Override
        public long getItemId(int id) {
            return id;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                /*实例化ImageView对象*/
                imageView = new ImageView(mContext);
                /* 设置ImageView对象布局，设置View的height和width */
                imageView.setLayoutParams(new GridView.LayoutParams(500, 500));
                /* 设置边界对齐*/
                imageView.setAdjustViewBounds(false);
                /* 按比例同意缩放图片（保持图片的尺寸比例）*/
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                /* 设置间距*/
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(ImageUtils.base64ToBitmap(MainActivity.photoList.get(position).getContent()));
            return imageView;
        }
    }
}