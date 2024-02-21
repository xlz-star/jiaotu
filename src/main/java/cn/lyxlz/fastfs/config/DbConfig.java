package cn.lyxlz.fastfs.config;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;
@Configuration
public class DbConfig {
    /**
     * 配置数据源
     */
    @Bean
    public DataSource db(@Inject("${datasource}") HikariDataSource ds) {
        return ds;
    }
}
