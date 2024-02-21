package cn.lyxlz.fastfs.constant;

/**
 * HTTP 类型枚举
 *
 * @author xlz
 * @date 2024/01/20
 */
public enum HttpTypeEnum {
    HTTP("http://"),
    HTTPS("https://");

    private String name;

    HttpTypeEnum(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
