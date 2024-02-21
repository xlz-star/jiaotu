package cn.lyxlz.fastfs.scheduled;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.entity.FileVO;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.sqltoy.annotation.Db;
import org.noear.solon.scheduling.annotation.Scheduled;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class ClearSchedule {

    @Inject
    System system;

    @Db
    SqlToyLazyDao dao;

    /**
     * 清理空文件夹，每天执行一次
     */
    @Scheduled(cron = "0 0 0 0/1 * ?")
    public void clearEmptyDir() throws IOException {
        if (!system.getMaster()) {
            log.debug("开始清理空文件夹");
            String fileDir = system.getFileDir();
            try (Stream<Path> paths = Files.walk(Paths.get(fileDir))){
                paths.forEach(f -> {
                    List<FileVO> fileVOS = dao.findEntity(FileVO.class,
                            EntityQuery.create()
                                    .where("#[parent_path = ?]")
                                    .values(FileService.SLASH + f.getFileName()));
                    if (ObjUtil.isEmpty(fileVOS)) {
                        FileUtil.del(f);
                    }
                });
            }

        }
    }
}
