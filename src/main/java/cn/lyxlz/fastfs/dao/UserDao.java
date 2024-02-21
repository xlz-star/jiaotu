package cn.lyxlz.fastfs.dao;

import cn.lyxlz.fastfs.entity.UserVO;

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
     * @return {@link List}<{@link UserVO}>
     */
    List<UserVO> getUser(UserVO user);

    /**
     * 获取用户信息
     *
     * @param uname
     * @return
     */
    List<UserVO> getUser(String uname);

    /**
     * 保存用户信息
     *
     * @param user
     * @return {@link Integer}
     */
    Object saveUser(UserVO user);
}
