package cn.lyxlz.fastfs.controller;

import cn.lyxlz.fastfs.annotation.Login;
import cn.lyxlz.fastfs.entity.UserVO;
import cn.lyxlz.fastfs.service.LoginService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.ModelAndView;

import java.util.Map;

@Controller
public class LoginController {

    @Inject
    LoginService loginService;



    @Get
    @Mapping("/")
    @Login
    public ModelAndView index() {
        return loginService.index();
    }

    @Get
    @Mapping("/login")
    public ModelAndView loginPage() {
        return new ModelAndView().view("login.html");
    }

    @Get
    @Mapping("/register")
    public ModelAndView registerPage() {
        return new ModelAndView().view("register.html");
    }

    @Get
    @Mapping("/api/info")
    public Map<String, Object> info() {
        return loginService.info();
    }

    @Post
    @Mapping("/api/login")
    public Map<String, Object> login(UserVO user) {
        return loginService.login(user);
    }

    @Post
    @Mapping("/api/register")
    public Map<String, Object> register(UserVO user) {
        return loginService.register(user);
    }

    @Get
    @Mapping("/api/loginOut")
    @Login
    public ModelAndView loginOut() {
        return loginService.loginOut();
    }

    @Get
    @Mapping("/404")
    public ModelAndView notFound() {
        return new ModelAndView("404.html");
    }
}
