package cn.lyxlz.fastfs.dao.impl;

import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.User;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.sqltoy.annotation.Db;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;

import java.util.List;

@Component
public class UserDaoImpl implements UserDao {

    @Db
    SqlToyLazyDao dao;

    @Override
    public List<User> getUser(User user) {
        String uname = user.getUname();
        String pwd = user.getPwd();
        return dao.findEntity(User.class,
                EntityQuery.create().where("#[uname = ?] #[ and passwd = ?]").values(uname, pwd));
    }

    @Override
    public Integer saveUser(User user) {
        return (Integer) dao.save(user);
    }
}
