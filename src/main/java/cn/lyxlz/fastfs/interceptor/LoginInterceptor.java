package cn.lyxlz.fastfs.annotation;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.handle.ModelAndView;

import java.util.Map;

/**
 * 登录拦截器
 *
 * @author xlz
 * @date 2023/09/02
 */
@Slf4j
public class LoginInterceptor implements Interceptor {

    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        // 拦截处理
        Object invoke = inv.invoke();
        if (invoke instanceof ModelAndView) {
            ModelAndView modelAndView = (ModelAndView) invoke;
            // 如果用户未登录则跳转到登录页面
            if (!StpUtil.isLogin()) {
                modelAndView.view("login.html");
            }
            return modelAndView;
        } else if (invoke instanceof Map<?, ?>) {
            return invoke;
        }
        return null;
    }
}