package cn.lyxlz.fastfs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.sql.Types;

/**
 * 用户实体
 *
 * @author xlz
 * @date 2023/09/02
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Entity(tableName = "user")
public class User implements Serializable {
    private static final long serialVersionUID = -5120346456991155896L;

    /**
     * 用户id
     */
    @Id
    @Column(name = "uid", type = Types.INTEGER)
    private Integer uid;

    /**
     * 账户
     */
    @Column(name = "uname", type = Types.VARCHAR)
    private String uname;

    /**
     * 密码
     */
    @Column(name = "passwd", type = Types.VARCHAR)
    private String pwd;

    /**
     * 权限
     */
    @Column(name = "aid", type = Types.VARCHAR)
    private String aid;

    /**
     * 存储路径
     */
    @Column(name = "path", type = Types.VARCHAR)
    private String path;
}
