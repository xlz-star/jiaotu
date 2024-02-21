package cn.lyxlz.fastfs.service;

import cn.lyxlz.fastfs.entity.UserVO;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;

import java.util.Map;

/**
 * 登录服务
 *
 * @author xlz
 * @date 2023/09/02
 */
public interface LoginService {
    /**
     * 首页访问
     *
     * @param ctx
     * @return
     */
    ModelAndView index();

    /**
     * 登录提交认证
     *
     * @param user 用户
     */
    Map<String, Object> login(UserVO user);

    /**
     * 返回节点数和容量
     * @return
     */
    Map<String, Object> info();

    /**
     * 注册新用户
     *
     * @param user 用户
     */
    Map<String, Object> register(UserVO user);

    /**
     * 注销登录
     *
     * @return {@link ModelAndView}
     */
    ModelAndView loginOut();
}
