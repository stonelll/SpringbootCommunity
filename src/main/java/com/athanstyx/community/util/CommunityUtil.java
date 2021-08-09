package com.athanstyx.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {


    //生成随机字符串
    public static String generaterUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }


    /*
     * MD5加密
     * 特点:
     * 1.只能加密不能解密
     * 2.相同字符串加密后的字符串相同
     *   fe: hello -> abc123def456
     * 所以在我们的user表里面我们都有一个salt,每次我们加密前都会给这个用户加上我们的salt再进行加密
     *   fe: hello + 3e5a1(salt) -> ada21wsd312azc1(MD5)
     *   所以这样就会保证安全性
     *  这个key也就是原本要加密的字符串外加我们的salt(随机生成的字符串)
     * */
    public static String MD5(String key) {
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "zhansan");
        map.put("age", 25);
        System.out.println(getJSONString(0,"ok",map));
    }

}
