package cn.lyxlz.fastfs.annotation;

import cn.lyxlz.fastfs.interceptor.LoginInterceptor;
import org.noear.solon.annotation.Around;

import java.lang.annotation.*;

/**
 * 登录
 *
 * @author xlz
 * @date 2023/09/02
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Login {
}
