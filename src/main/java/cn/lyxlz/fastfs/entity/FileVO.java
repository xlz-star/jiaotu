package cn.lyxlz.fastfs.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.BusinessId;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Types;

@Data
@Accessors(chain = true)
@Entity(tableName = "public.file")
public class FilePO implements Serializable {

    private static final long serialVersionUID = 1178358383211437560L;

    // TODO @Column(name = "fid", type = Types.INTEGER)
    private Integer fid;

    @Column(name = "fname", type = Types.VARCHAR)
    private String fname;

    @Column(name = "real_path", type = Types.VARCHAR)
    private String realPath;

    @Column(name = "uid", type = Types.INTEGER)
    private Integer uid;

    @Column(name = "parent_path", type = Types.VARCHAR)
    private String parentPath;

    @Column(name = "update_time", type = Types.DATE)
    private Date updateTime;

    @Column(name = "is_dir", type = Types.BOOLEAN)
    private Boolean dir;

    @Column(name = "ftype", type = Types.VARCHAR)
    private String ftype;
}
