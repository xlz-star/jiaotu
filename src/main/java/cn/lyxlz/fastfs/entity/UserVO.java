package cn.lyxlz.fastfs.entity;

import lombok.Data;
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
@Accessors(chain = true)
@Entity(tableName = "public.user")
public class UserPO implements Serializable {
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
    private String passwd;

    /**
     * 用户身份
     */
    @Column(name = "identify", type = Types.INTEGER)
    private Integer identify;

}
