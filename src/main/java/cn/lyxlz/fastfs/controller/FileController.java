package cn.lyxlz.fastfs.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.lyxlz.fastfs.annotation.Login;
import cn.lyxlz.fastfs.service.FileService;
import cn.lyxlz.fastfs.service.impl.FileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;

import java.sql.SQLException;
import java.util.Map;

@Controller
@Slf4j
public class FileController {

    @Inject
    FileService fileService;

    @Get
    @Login
    @Mapping("/")
    public ModelAndView index(ModelAndView modelAndView) {
        return modelAndView.view("index.html");
    }

    @Post
    @Login
    @Mapping("/file/upload")
    public Map<String, Object> upload(UploadedFile file, String curPos) {
        return fileService.upload(file, curPos);
    }

    @Get
    @Login
    @Mapping("/api/list")
    public Map<String, Object> list(String dir, String accept, String exts) throws SQLException {
        return fileService.list(dir, accept, exts);
    }

    @Get
    @Login
    @Mapping("/file")
    public ModelAndView file(String p, int d) {
        return fileService.file(p, d);
    }

    @Get
    @Login
    @Mapping("/share/file")
    public ModelAndView shareFile(String sid, int d) {
        return fileService.shareFile(sid, d);
    }

    @Get
    @Login
    @Mapping("/share/file/sm")
    public ModelAndView shareFileSm(String sid) {
        return fileService.shareFileSm(sid);
    }

    @Get
    @Login
    @Mapping("/file/sm")
    public ModelAndView fileSm(String p) {
        return fileService.fileSm(p);
    }

    @Get
    @Login
    @Mapping("/api/mkdir")
    public Map<String, Object> mkdir(String curPos, String dirName) {
        return fileService.mkdir(curPos, dirName);
    }

    @Post
    @Login
    @Mapping("/api/share")
    public Map<String, Object> share(String file, int time) {
        return fileService.share(file, time);
    }

    @Get
    @Login
    @Mapping("/share")
    public ModelAndView sharePage(String sid) {
        return fileService.sharePage(sid);
    }

    @Get
    @Mapping("/api/del")
    public Map<String, Object> del(String file) {
        return fileService.del(file);
    }

    @Get
    @Mapping("/api/rename")
    public Map<String, Object> rename(String oldFile, String newFile) {
        return fileService.rename(oldFile, newFile);
    }
}
