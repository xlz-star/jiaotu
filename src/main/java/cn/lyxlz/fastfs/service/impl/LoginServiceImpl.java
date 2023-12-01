package cn.lyxlz.fastfs.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.entity.User;
import cn.lyxlz.fastfs.service.FileService;
import cn.lyxlz.fastfs.service.LoginService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ModelAndView;

import java.util.List;

/**
 * 登录服务实现
 *
 * @author xlz
 * @date 2023/09/05
 */
@Component
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Inject
    System system;

    @Inject
    UserDao userDao;

    @SneakyThrows
    @Override
    public ModelAndView auth(User user) {
        ModelAndView modelAndView = new ModelAndView();
        log.debug("用户 : {} 已登录", user.getUname());
        if (ObjUtil.isEmpty(user)) {
            modelAndView.put("code", 401);
            modelAndView.view("login.html");
            return modelAndView;
        }
        List<User> users = userDao.getUser(user);
        if (ObjUtil.isNotEmpty(users)) {
            // 账号密码正确
            User loginUser = users.get(0);
            StpUtil.login(loginUser.getUname());
            modelAndView.put("username", loginUser.getUname());
            return modelAndView.view("index.html");
        }
        modelAndView.put("code", 401);
        modelAndView.view("login.html");
        return modelAndView;
    }

    @SneakyThrows
    @Override
    public ModelAndView register(User user) {
        ModelAndView modelAndView = new ModelAndView();
        log.debug("用户 {} 正在注册", user);
        String userPath = system.getFileDir() + FileService.SLASH + user.getUname();
        user.setPath(userPath)
                .setAid("1,2,3,4");
        FileUtil.mkdir(userPath);
        Integer id = userDao.saveUser(user);
        if (ObjUtil.isNotEmpty(id)) {
            modelAndView.put("code", 200);
            modelAndView.put("msg", "注册成功");
            return modelAndView.view("register.html");
        }
        modelAndView.put("code", 401);
        modelAndView.put("msg", "用户已存在");
        return modelAndView.view("register.html");
    }

    @Override
    public ModelAndView loginOut() {
        ModelAndView modelAndView = new ModelAndView();
        StpUtil.logout();
        return modelAndView.view("login.html");
    }
}
