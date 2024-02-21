package cn.lyxlz.fastfs.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 系统配置实体
 *
 * @author xlz
 * @date 2023/09/02
 */
@Data
@Accessors(chain = true)
public class System {
    private static final String SLASH = "/";

    /**
     * 文件目录
     */
    private String fileDir;

    /**
     * 是否使用uuid
     */
    private Boolean uuidName;

    /**
     * 是否使用缩略图
     */
    private Boolean useSm;

    /**
     * 是否使用nginx
     */
    private Boolean useNginx;

    /**
     * nginx url
     */
    private String nginxUrl;

    /**
     * 管理员用户名
     */
    private String uname;

    /**
     * 管理员密码
     */
    private String pwd;

    /**
     * 域名
     */
    private String domain;

    /**
     * 是否是主节点
     */
    private Boolean master;

    private List<String> works;
}
