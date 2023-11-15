package cn.lyxlz.fastfs.dao;

import cn.lyxlz.fastfs.entity.User;

import java.util.List;

/**
 * @author xlz
 * @date 2023/11/15
 * 用户操作dao
 */
public interface UserDao {
    /**
     * 获取用户信息
     *
     * @param user
     * @return {@link List}<{@link User}>
     */
    List<User> getUser(User user);

    /**
     * 保存用户信息
     *
     * @param user
     * @return {@link Integer}
     */
    Integer saveUser(User user);
}
