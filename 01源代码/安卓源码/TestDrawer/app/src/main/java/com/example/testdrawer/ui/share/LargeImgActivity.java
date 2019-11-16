package com.example.testdrawer.ui.share;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.example.testdrawer.MainActivity;
import com.example.testdrawer.R;
import com.example.testdrawer.bean.Photo;
import com.example.testdrawer.utils.GsonUtils;
import com.example.testdrawer.utils.HttpUtil;
import com.example.testdrawer.utils.ImageUtils;


/**
 * 点击查看大图页面
 */
public class LargeImgActivity extends AppCompatActivity {

    private int position;
    private Photo photo;

    /**
     * 显示大图以及绘制人脸位置
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_photo_entry);
        Bundle bundle = this.getIntent().getExtras();
        if(bundle!=null){
            position = bundle.getInt("position"); //第几张图片
        }

        ImageView img = (ImageView)this.findViewById(R.id.large_image );
        photo = (Photo) MainActivity.photoList.get(position);
        String img_base64 = photo.getContent();
        Bitmap img_bitmap = ImageUtils.base64ToBitmap(img_base64);
        int w = img_bitmap.getWidth();
        int h = img_bitmap.getHeight();
        Paint paint = new Paint();//画笔
        paint.setColor(Color.RED);//设置颜色
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);//创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(img_bitmap, 0, 0, null);//在 0，0坐标开始画入src
        //在src的右下角画入
        for (int i = 0 ; i < ShareFragment.face_list.size() ; i++){
            cv.drawRect(ShareFragment.face_list.get(i),paint);
        }
        img.setImageBitmap(newb);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                finish();
            }
        });
    }
}
