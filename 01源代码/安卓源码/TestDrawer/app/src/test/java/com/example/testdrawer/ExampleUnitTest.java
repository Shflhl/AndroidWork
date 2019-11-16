package com.example.testdrawer;

import com.example.testdrawer.utils.Base64Util;
import com.example.testdrawer.utils.FileUtil;
import com.example.testdrawer.utils.GsonUtils;
import com.example.testdrawer.utils.HttpUtil;

import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 测试类，测试人脸检测API
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    /**
     * 人脸检测 检测图片中最大脸的位置
     */
    @Test
    public void faceDetection(){
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/object_detect";
        try {
            String imgParam = URLEncoder.encode("[图片Base64字符串]", "UTF-8");
            String param = "image=" + imgParam + "&with_face=" + 1;
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.6ac6cac20c6ddd02effa33e9d53d10d6.2592000.1575943062.282335-17737224";

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 人脸对比 两张图片对比相似度
     */
    @Test
    public void faceMatch(){
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", "[图片Base64字符串]");
            map.put("image_type", "BASE64");

            Map<String, Object> map1 = new HashMap<>();
            // 本地文件路径
            String filePath1 = "08.jpg";
            byte[] imgData1 = FileUtil.readFileByBytes(filePath1);
            String imgStr1 = Base64Util.encode(imgData1);
            map1.put("image", imgStr1);
            map1.put("image_type", "BASE64");


            String param = "["+ GsonUtils.toJson(map)+","+GsonUtils.toJson(map1)+"]";

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208";

            String result = HttpUtil.post(url, accessToken, "application/json", param);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸注册
     */
    @Test
    public void faceRegistered(){
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", "[图片Base64字符串]");
            map.put("group_id", "group_repeat");
            map.put("user_id", "user1");
            map.put("user_info", "abc");
            map.put("liveness_control", "NORMAL");
            map.put("image_type", "FACE_TOKEN");
            map.put("quality_control", "LOW");

            String param = GsonUtils.toJson(map);

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208";

            String result = HttpUtil.post(url, accessToken, "application/json", param);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  人脸搜索
     */
    @Test
    public void  faceSearch(){
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/search";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", "[图片Base64字符串]");
            map.put("liveness_control", "NORMAL");
            map.put("group_id_list", "group_repeat,group_233");
            map.put("image_type", "FACE_TOKEN");
            map.put("quality_control", "LOW");

            String param = GsonUtils.toJson(map);

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208";

            String result = HttpUtil.post(url, accessToken, "application/json", param);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208"
     */
    @Test
    public String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.err.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            System.err.println("result:" + result);
            JSONObject jsonObject = new JSONObject(result);
            String access_token = jsonObject.getString("access_token");
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }
}