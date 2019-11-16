package com.example.testdrawer.utils;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.List;

/**
 *  字符串转换为Json对象
 */
public class JsonUtils {

    /**
     * Json 数组字符串转化为List对象
     * @param text
     * @param clazz
     * @return List对象
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        try {
            return JSON.parseArray(text, clazz);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
