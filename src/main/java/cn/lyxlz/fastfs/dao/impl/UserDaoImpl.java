package cn.lyxlz.fastfs.dao.impl;

import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.UserVO;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.util.List;

@Component
public class UserDaoImpl implements UserDao {

    @Db
    SqlToyLazyDao dao;

    @Override
    public List<UserVO> getUser(UserVO user) {
        String uname = user.getUname();
        String pwd = user.getPasswd();
        return dao.findEntity(UserVO.class,
                EntityQuery.create().where("#[uname = ?] #[ and passwd = ?]").values(uname, pwd));
    }

    public List<UserVO> getUser(String uname) {
        return dao.findEntity(UserVO.class,
                EntityQuery.create().where("#[uname = ?]").values(uname));
    }

    @Override
    public Object saveUser(UserVO user) {
        return dao.save(user);
    }
}
