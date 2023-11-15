package cn.lyxlz.fastfs.annotation;

import org.noear.solon.annotation.Around;

import java.lang.annotation.*;

/**
 * 登录
 *
 * @author xlz
 * @date 2023/09/02
 */
@Documented
@Around(LoginInterceptor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Login {
}
