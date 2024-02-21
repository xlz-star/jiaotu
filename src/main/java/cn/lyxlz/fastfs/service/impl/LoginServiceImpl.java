package cn.lyxlz.fastfs.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.entity.UserVO;
import cn.lyxlz.fastfs.service.DistributService;
import cn.lyxlz.fastfs.service.FileService;
import cn.lyxlz.fastfs.service.LoginService;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import cn.lyxlz.fastfs.util.ResUtil;
import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.data.annotation.Tran;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Inject
    DistributService distributService;

    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        distributService.findNode();
        // 返回在线节点数和容量
        modelAndView.put("username", StpUtil.getLoginId().toString());
        modelAndView.view("index.html");
        return modelAndView;
    }

    @SneakyThrows
    @Override
    public Map<String, Object> login(UserVO user) {
        ModelAndView modelAndView = new ModelAndView();
        if (ObjUtil.isEmpty(user)) {
            modelAndView.put("code", 401);
            modelAndView.view("login.html");
            return ResUtil.getRS(401, "账号和密码不能为空");
        }
        List<UserVO> users = userDao.getUser(user);
        if (ObjUtil.isNotEmpty(users)) {
            // 账号密码正确
            UserVO loginUser = users.getFirst();
            StpUtil.login(loginUser.getUname());
            String userJson = NodeUtil.toJson(loginUser);
            // 登录信息登录期间缓存七天
            CacheUtil.put(user.getUname(), userJson, 10080);
            return ResUtil.getRS(200, "登录成功");
        }
        return ResUtil.getRS(401, "账号或密码错误");
    }

    @Override
    public Map<String, Object> info() {
        HashMap<String, Object> res = new HashMap<>();
        res.put("nodeNum", NodeUtil.size());
        res.put("free", String.format("%.3f", NodeUtil.sumFree() / 1024 / 1024.0));
        return ResUtil.getRS(200, JSON.toJSONString(res));
    }

    @SneakyThrows
    @Override
    @Tran
    public Map<String, Object> register(UserVO user) {
        String userPath = system.getFileDir() + FileService.SLASH + user.getUname();
        FileUtil.mkdir(userPath);
        List<UserVO> users = userDao.getUser(user);
        if (ObjUtil.isEmpty(users)) {
            user.setUid(UUID.randomUUID().toString());
            user.setIdentity(1);
            userDao.saveUser(user);
            return ResUtil.getRS(200, "注册成功");
        }
        return ResUtil.getRS(401, "用户已存在");
    }

    @Override
    public ModelAndView loginOut() {
        ModelAndView modelAndView = new ModelAndView();
        // 登出删除缓存
        Object loginId = StpUtil.getLoginId();
        CacheUtil.remove(loginId);
        StpUtil.logout();
        return modelAndView.view("login.html");
    }
}
