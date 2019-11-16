package com.example.testdrawer.utils;

import java.net.URLEncoder;

/**
 * This method inputs a number from the user.
 * 本类分装了人脸检测所需要的代码
 */
public class ObjectDetect {
    /**
     * 重要提示代码中所需工具类
     * FileUtil,Base64Util,HttpUtil,GsonUtils请从
     * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
     * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
     * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
     * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
     * 下载
     */
    public static String objectDetect(String content) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/object_detect";
        try {
            // 本地文件路径
            //String filePath = "[本地文件路径]";
            //byte[] imgData = FileUtil.readFileByBytes(filePath);
            //String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(content, "UTF-8");

            String param = "image=" + imgParam + "&with_face=" + 1;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.6ac6cac20c6ddd02effa33e9d53d10d6.2592000.1575943062.282335-17737224";

            String result = HttpUtil.post(url, accessToken, param);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
