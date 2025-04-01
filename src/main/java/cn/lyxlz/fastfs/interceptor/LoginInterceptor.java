package cn.lyxlz.fastfs.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.lyxlz.fastfs.annotation.Login;
import cn.lyxlz.fastfs.annotation.Waf;
import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.UserVO;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import cn.lyxlz.fastfs.util.ResUtil;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Action;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;
import com.alibaba.fastjson2.JSON;

import cn.lyxlz.fastfs.entity.System;
import org.noear.solon.core.util.KeyValues;
import org.noear.solon.core.util.MultiMap;

import java.util.List;

/**
 * 登录拦截器
 *
 * @author xlz
 * @date 2023/09/02
 */
@Component
@Slf4j
public class LoginInterceptor implements RouterInterceptor {

    @Inject
    UserDao userDao;

    @Inject
    System system;

    @Override
    public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        // 如果是主节点访问则放行
        String userJson = ctx.paramOrDefault("user", null);
        if (ObjUtil.isNotEmpty(userJson)) {
            UserVO userVO = NodeUtil.toUser(userJson);
            // 从数据库中检测用户信息是否正确
            if ("master".equals(ctx.param("who"))) {
                List<UserVO> checkUser = userDao.getUser(userVO);
                if (ObjUtil.isEmpty(checkUser)) {
                    ctx.render(ResUtil.getRS(404, "用户校验失败"));
                }
                // 登录信息缓存七天
                StpUtil.login(userVO.getUname());
                CacheUtil.put(userVO.getUname(), userJson, 10080);
                chain.doIntercept(ctx, mainHandler);
            }
        } else {
            Action action = (mainHandler instanceof Action ? (Action) mainHandler : null);
            if (ObjUtil.isNotNull(action)) {
                if (system.getUseWaf()) {
                    Waf waf = action.method().getAnnotation(Waf.class);
                    if (ObjUtil.isNotNull(waf)) {
                        String prediction;
                        // 拼接参数
                        StringBuffer s = new StringBuffer("?");
                        MultiMap<String> paramMap = ctx.paramMap();
                        ctx.paramNames().forEach(name -> s.append(name).append("=").append(paramMap.get(name)).append("&"));
                        try (HttpResponse execute = HttpRequest.post(system.getWafUrl())
                                .header("Content-Type", "application/json")
                                .body(String.format("{\"url\": \"%s\"}", s))
                                .execute()) {
                            String res = execute
                                    .body();
                            prediction = JSON.parseObject(res).get("prediction").toString();
                        }
                        if (prediction.equals("恶意请求")) {
                            log.warn("检测到恶意请求，已拦截：{}{}", ctx.uri(), s);
                            ctx.redirect("/404");
                        }
                    }
                }

                Login login = action.method().getAnnotation(Login.class);
                if (ObjUtil.isNotNull(login)) {
                    // 如果用户未登录则跳转到登录页面
                    if (!StpUtil.isLogin()) {
                        ctx.redirect("/login");
                    } else {
                        chain.doIntercept(ctx, mainHandler);
                    }
                } else {
                    chain.doIntercept(ctx, mainHandler);
                }
            } else {
                chain.doIntercept(ctx, mainHandler);
            }
        }
    }
}