package cn.lyxlz.fastfs.config;

import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.service.FileService;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * 系统配置
 *
 * @author xlz
 * @date 2023/09/02
 */
@Configuration
public class SystemConfig {

    @Bean
    public System system(
            @Inject("${fs.dir}") String fileDir,
            @Inject("${fs.uuidName}") Boolean uuidName,
            @Inject("${fs.useSm}") Boolean useSm,
            @Inject("${fs.useNginx}") Boolean useNginx,
            @Inject("${fs.nginxUrl}") String nginxUrl,
            @Inject("${fs.master}") Boolean master,
            @Inject("${admin.uname}") String uname,
            @Inject("${admin.pwd}") String pwd,
            @Inject("${domain}") String domain,
            @Inject("${fs.works}") List<String> works
    ) {
        System system = new System();
        if (ObjUtil.isEmpty(fileDir)) {
            fileDir = "." + FileService.SLASH;
        }
        system.setFileDir(fileDir)
                .setUuidName(uuidName)
                .setUseSm(useSm)
                .setUseNginx(useNginx)
                .setUname(uname)
                .setNginxUrl(nginxUrl)
                .setPwd(pwd)
                .setDomain(domain)
                .setMaster(master)
                .setWorks(works);
        return system;
    }

}
