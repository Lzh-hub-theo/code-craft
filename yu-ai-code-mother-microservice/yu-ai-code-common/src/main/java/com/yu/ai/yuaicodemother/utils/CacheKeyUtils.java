package com.yu.ai.yuaicodemother.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存 key 生成工具类
 */
public class CacheKeyUtils {

    /**
     * 根据对象生成缓存 key（JSON+MD5）
     *
     * @param obj 要生成 key 的对象
     * @return MD5哈希后的缓存 key
     */
    public static String generateKey(Object obj) {
        if (obj == null) {
            return DigestUtil.md5Hex("null");
        }
        // 先转 json,再转md5
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
