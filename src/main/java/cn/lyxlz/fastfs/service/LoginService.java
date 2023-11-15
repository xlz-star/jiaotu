package cn.lyxlz.fastfs.service;

import cn.lyxlz.fastfs.entity.User;
import org.noear.solon.core.handle.ModelAndView;

/**
 * 登录服务
 *
 * @author xlz
 * @date 2023/09/02
 */
public interface LoginService {

    /**
     * 登录提交认证
     *
     * @param user 用户
     * @return {@link ModelAndView}
     */
    ModelAndView auth(User user);

    /**
     * 注册新用户
     *
     * @param user 用户
     * @return {@link ModelAndView}
     */
    ModelAndView register(User user);

    /**
     * 注销登录
     *
     * @return {@link ModelAndView}
     */
    ModelAndView loginOut();
}
