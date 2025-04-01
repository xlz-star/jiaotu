package cn.lyxlz.fastfs.annotation;

import java.lang.annotation.*;

/**
 * 需要拦截的接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Waf {
}
