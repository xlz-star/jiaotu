package cn.lyxlz.fastfs.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回前端数据处理工具
 *
 * @author xlz
 * @date 2023/09/09
 */
public class ResUtil {
    /**
     * 封装返回结果
     *
     * @param code
     * @param msg
     * @param url
     * @return Map
     */
    public static Map<String, Object> getRS(int code, String msg, String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        if (url != null) {
            map.put("url", url);
        }
        return map;
    }

    /**
     * 封装返回结果
     *
     * @param code
     * @param msg
     * @return Map
     */
    public static Map<String, Object> getRS(int code, String msg) {
        return getRS(code, msg, null);
    }

}
