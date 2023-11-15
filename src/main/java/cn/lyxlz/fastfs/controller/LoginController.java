package cn.lyxlz.fastfs.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.lyxlz.fastfs.entity.User;
import cn.lyxlz.fastfs.service.LoginService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.ModelAndView;

import java.util.Map;

@Controller
public class LoginController {

    @Inject
    LoginService loginService;

    @Post
    @Mapping("/auth")
    public ModelAndView auth(User user) {
        return loginService.auth(user);
    }

    @Get
    @Mapping("/register")
    public ModelAndView registerPage() {
        return new ModelAndView().view("register.html");
    }

    @Post
    @Mapping("/api/register")
    public ModelAndView register(User user) {
        return loginService.register(user);
    }

    @Get
    @Mapping("/api/loginOut")
    public ModelAndView loginOut() {
        return loginService.loginOut();
    }
}
