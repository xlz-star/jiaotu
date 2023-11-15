package cn.lyxlz.fastfs.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.solon.integration.SaTokenInterceptor;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

@Configuration
public class SaTokenConfig {
    @Bean(index = -100) //（低值优先）
    public SaTokenInterceptor saTokenInterceptor() {
        return new SaTokenInterceptor()
                // 指定 拦截路由 和 放行路由
                .addInclude("/**")
                .addExclude("/favicon.ico")
                .addExclude("/assets/*")
                .addExclude("/images/*")
                .addExclude("/register")
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(req -> {
                    // 设置一些安全响应头
                    SaHolder.getResponse()
                            // 服务器名称
                            .setServer("fast-fs")
                            // 是否可以在iframe显示视图
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            // 是否启用浏览器默认XSS防护
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            // 禁用浏览器内容嗅探
                            .setHeader("X-Content-Type-Options", "nosniff");
                });
    }
}
