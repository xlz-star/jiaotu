package cn.lyxlz.fastfs.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.lyxlz.fastfs.annotation.Login;
import cn.lyxlz.fastfs.annotation.Waf;
import cn.lyxlz.fastfs.service.FileService;
import cn.lyxlz.fastfs.service.impl.FileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.sagacity.sqltoy.config.annotation.OneToMany;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class FileController {

    @Inject
    FileService fileService;

    @Get
    @Login
    @Mapping("/api/search")
    public Map<String, Object> search(String fileName, String dir, String accept, String exts) {
        return fileService.search(fileName, dir, accept, exts);
    }

    @Get
    @Login
    @Mapping("/")
    public ModelAndView index(ModelAndView modelAndView) {
        modelAndView.put("username", StpUtil.getLoginId().toString());
        return modelAndView.view("index.html");
    }

    @Post
    @Login
    @Mapping("/file/recv")
    public Map<String, Object> recv(UploadedFile file, String curPos) {
        return fileService.recv(file, curPos);
    }

    @Post
    @Login
    @Mapping("/file/upload")
    public Map<String, Object> upload(UploadedFile file, String curPos) {
        return fileService.upload(file, curPos);
    }

    @Get
    @Login
    @Mapping("/upload")
    public ModelAndView uploadPage() {
        return new ModelAndView().view("upload.html");
    }

    @Get
    @Login
    @Mapping("/search")
    public ModelAndView searchPage(String fileName) {
        HashMap<String, String> model = new HashMap<>();
        model.put("fileName", fileName);
        model.put("username", StpUtil.getLoginId().toString());
        return new ModelAndView("search.html", model);
    }

    @Get
    @Login
    @Mapping("/file/findRealPath")
    public Map<String, Object> findRealPath(String f) {
        return fileService.findRealPath(f);
    }

    @Get
    @Login
    @Waf
    @Mapping("/api/list")
    public Map<String, Object> list(String dir, String accept, String exts) throws SQLException {
        return fileService.list(dir, accept, exts);
    }

    @Get
    @Mapping("/file")
    public ModelAndView file(String p, int d) {
        return fileService.file(p, d);
    }

    @Get
    @Mapping("/share/file")
    public ModelAndView shareFile(String sid, int d) {
        return fileService.shareFile(sid, d);
    }

    @Get
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
    public Map<String, Object> share(String file, String user, int time) {
        return fileService.share(file, user, time);
    }

    @Post
    @Mapping("/api/remoteShare")
    public Map<String, Object> remoteShare(String file, int time) {
        return fileService.remoteShare(file, time);
    }

    @Get
    @Mapping("/share")
    public ModelAndView sharePage(String sid) {
        return fileService.sharePage(sid);
    }

    @Get
    @Mapping("/api/remoteDel")
    public Map<String, Object> remoteDel(String file) {
        return fileService.remoteDel(file);
    }

    @Get
    @Mapping("/api/remoteDelDir")
    public Map<String, Object> remoteDelDir(String dirName) {
        return fileService.remoteDelDir(dirName);
    }

    @Get
    @Mapping("/api/rename")
    public Map<String, Object> rename(String parentPath, String oldFile, String newFile) {
        return fileService.rename(parentPath, oldFile, newFile);
    }

    @Get
    @Mapping("/api/renameDir")
    public Map<String, Object> renameDir(String parentPath, String oldFile, String newFile) {
        return fileService.renameDir(parentPath, oldFile, newFile);
    }

    @Post
    @Mapping("/api/del")
    public Map<String, Object> del(String file) {
        return fileService.del(file);
    }

}
