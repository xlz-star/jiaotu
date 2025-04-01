package cn.lyxlz.fastfs.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.lyxlz.fastfs.constant.FileTypeEnum;
import cn.lyxlz.fastfs.constant.HttpTypeEnum;
import cn.lyxlz.fastfs.dao.FileDao;
import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.*;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.service.DistributService;
import cn.lyxlz.fastfs.service.FileService;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import cn.lyxlz.fastfs.util.ResUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static cn.lyxlz.fastfs.util.ResUtil.*;

/**
 * 文件服务实现
 *
 * @author xlz
 * @date 2023/09/06
 */
@Slf4j
@Component
public class FileServiceImpl implements FileService {
    /**
     * 系统配置
     */
    @Inject
    System system;

    @Inject
    UserDao userDao;

    @Inject
    FileDao fileDao;

    @Inject
    DistributService distributService;

    @SneakyThrows
    @Override
    public Map<String, Object> search(String fileName, String dir, String accept, String exts) {
        Map<String, Object> list = list("/", accept, exts);
        if (!((int) list.get("code") == 200)) {
            return list;
        }
        List<Map<String, Object>> data = (List<Map<String, Object>>) list.get("data");
        // 使用Steam流过滤出文件名中包含指定字符的文件
        List<Map<String, Object>> filteredData = data.stream()
                .filter(item -> item.get("name").toString().contains(fileName))
                .toList();
        list.put("data", filteredData);
        return list;
    }

    public Map<String, Object> recv(UploadedFile file, String curPos) {
        curPos = curPos.substring(1) + SLASH;
        String dir = curPos;
        String fileDir = system.getFileDir() + SLASH + dir;
        // 文件原始名称
        String fileName = file.getName();
        // 保存到磁盘
        File outFile = FileUtil.file(fileDir + fileName);
        try {
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            // 切换目录
            file.transferTo(outFile);
            log.debug("文件已保存: {}", outFile.getAbsoluteFile());
            // 上传成功后，将文件发送到合适的数据节点
            return getRS(200, "上传成功", outFile.getPath());
        } catch (IOException e) {
            log.debug(e.getMessage());
            return getRS(500, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> remoteDelDir(String dir) {
        // 找到要删除文件的真实路径
        String[] split = dir.split("/");
        StringBuilder dirBuilder = new StringBuilder(SLASH);
        for (int i = 0; i < split.length - 1; i++) {
            dirBuilder.append(split[i]);
        }
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO userVO = NodeUtil.toUser(userJson);
        List<FileVO> dirByName = fileDao.getDirByName(dirBuilder.toString(), userVO.getUid());
        if (ObjUtil.isNotEmpty(dirByName)) {
            FileVO fileVO = dirByName.getFirst();
            Long flag = 0L;
            flag += deleteDir(fileVO);
            if (flag > 0) {
                fileDao.deleteDirByDirName(fileVO.getFname());
                return ResUtil.getRS(200, "删除成功");
            }
        }
        return ResUtil.getRS(500, "文件未找到");
    }

    /**
     * 辅助递归删除文件夹
     *
     * @return
     */
    private Long deleteDir(FileVO dir) {
        if (dir.getDir()) {
            // 对于文件夹，先查看是否是空文件夹
            String dirName = ObjUtil.equals(SLASH, dir.getParentPath()) ?
                    dir.getParentPath() + dir.getViewPath()
                    : dir.getParentPath() + SLASH + dir.getViewPath();
            List<FileVO> files = fileDao.getFiles(dirName, dir.getUid());
            if (ObjUtil.isNotEmpty(files)) {
                for (FileVO file : files) {
                    deleteDir(file);
                }
                return fileDao.deleteDirByDirName(dir.getFname());
            } else {
                // 空文件夹直接删除
                return fileDao.deleteDirByDirName(dir.getFname());
            }
        } else {
            // 不是文件夹直接删除
            String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
            String realPath = dir.getRealPath();
            HashMap<String, Object> param = new HashMap<>();
            param.put("file", dir.getParentPath() + SLASH + dir.getFname());
            param.put("user", userJson);
            // 存入节点信息
            param.put("who", "master");
            String res = HttpUtil.post(realPath + "/api/del", param);
            JSONObject resJson = JSON.parseObject(res);
            if (ObjUtil.equals(resJson.get("code"), 200)) {
                return fileDao.deleteDirByDirName(dir.getFname());
            }
        }
        return 1L;
    }


    @Override
    public Map<String, Object> upload(UploadedFile file, String curPos) {
        String fileDir = system.getFileDir() + SLASH + curPos;
        // 文件原始名称
        String fileName = file.getName();
        int endIndex = fileName.lastIndexOf(".");
        String suffix = fileName.substring(endIndex + 1);
        String prefix = fileName.substring(0, Math.max(endIndex, 0));
        // 保存到磁盘
        File outFile;
        String path;
        if (ObjUtil.isNotEmpty(system.getUuidName()) && system.getUuidName()) {
            path = curPos + SLASH + UUID.randomUUID().toString().replaceAll("-", "") + fileName;
            outFile = FileUtil.file(fileDir + path);
        } else {
            int index = 1;
            path = curPos + fileName;
            outFile = FileUtil.file(fileDir + fileName);
            // 防止文件名重复，以数字后缀重命名
            while (outFile.exists()) {
                path = prefix + "(" + index + ")." + suffix;
                outFile = FileUtil.file(fileDir + path);
                index++;
            }
        }
        try {
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            // 切换目录
            file.transferTo(outFile);
            // 上传成功后，将文件发送到合适的数据节点
            distributService.sendResource(outFile, curPos);
            Map<String, Object> rs = getRS(200, "上传成功", path);
            // 生成缩略图，缩略图保存在本地加速显示
            if (ObjUtil.isNotEmpty(system.getUseSm()) && system.getUseSm()) {
                // 获取文件类型
                String contentType = FileTypeUtil.getType(outFile);
                if (contentType != null && contentType.startsWith("image/")) {
                    File smImg = new File(fileDir + "sm/" + path);
                    if (!smImg.getParentFile().exists()) {
                        smImg.getParentFile().mkdirs();
                    }
                    Thumbnails.of(outFile).scale(1f).outputQuality(0.25f).toFile(smImg);
                    rs.put("smUrl", "sm/" + path);
                }
                return rs;
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
            return getRS(500, e.getMessage());
        }
        return null;
    }

    @Override
    public ModelAndView file(String p, int d) {
        return getFile(p, d == 1, new ModelAndView());
    }

    @Override
    public Map<String, Object> findRealPath(String fileName) {
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO user = NodeUtil.toUser(userJson);
        List<FileVO> fileRealPath = fileDao.getFileRealPath(fileName, user.getUid());
        if (ObjUtil.isNotEmpty(fileRealPath)) {
            return ResUtil.getRS(200, fileRealPath.getFirst().getRealPath());
        }
        return ResUtil.getRS(500, "文件路径查找失败");
    }

    @Override
    public ModelAndView shareFile(String sid, int d) {
        return returnShareFileOrSm(sid, d == 1, new ModelAndView());
    }

    @Override
    public ModelAndView shareFileSm(String sid) {
        return returnShareFileOrSm(sid, false, new ModelAndView());
    }

    @Override
    public ModelAndView fileSm(String p) {
        return getFile(p, false, new ModelAndView());
    }

    @Override
    public Map<String, Object> list(String dir, String accept, String exts) throws SQLException {
        String[] mExts = null;
        if (ObjUtil.isNotEmpty(exts)) {
            mExts = exts.split(",");
        }
        HashMap<String, Object> rs = new HashMap<>();
        String username = (String) StpUtil.getLoginId();
        List<Map<String, Object>> dataList = new LinkedList<>();
        List<FileVO> listFiles;
        UserVO userVO = NodeUtil.toUser(CacheUtil.get(username).toString());
        listFiles = fileDao.getFileByUserId(dir, userVO.getUid());
        if (ObjUtil.isNotEmpty(listFiles)) {
            for (FileVO f : listFiles) {
                if ("sm".equals(f.getFname())) {
                    continue;
                }
                HashMap<String, Object> m = new HashMap<>();
                // 文件名称
                m.put("name", f.getViewPath());
                // 修改时间
                m.put("updateTime", f.getUpdateTime());
                // 是否是目录
                m.put("isDir", f.getDir());
                if (f.getDir()) {
                    // 文件类型
                    m.put("type", "dir");
                } else {
                    boolean flag = false;
                    if (cn.lyxlz.fastfs.util.FileTypeUtil.canOnlinePreview(f.getFtype())) {
                        flag = true;
                    }
                    m.put("preview", flag);
                    // 文件地址
                    m.put("url", (dir.isEmpty() ? dir : dir + SLASH) + f.getFname());
                    // 获取文件类型
                    String contentType = null;
                    String suffix = f.getFname().substring(f.getFname().lastIndexOf(".") + 1);
                    contentType = f.getFtype();
                    if (accept != null && !accept.trim().isEmpty() && !accept.equals("file")) {
                        if (contentType == null || !contentType.startsWith(accept + SLASH)) {
                            continue;
                        }
                    }
                    // 获取文件图标
                    m.put("type", getFileType(suffix, contentType));
                    // 是否有缩略图
                    String smUrl = "sm/" + (dir.isEmpty() ? dir : (dir + SLASH)) + f.getFname();
                    if (FileUtil.file(system.getFileDir() + smUrl).exists()) {
                        m.put("hasSm", true);
                        // 缩略图地址
                        m.put("smUrl", smUrl);
                    }
                }
                dataList.add(m);
            }
        }
        // 根据上传时间排序
        dataList.sort((o1, o2) -> {
            Date l1 = (Date) o1.get("updateTime");
            Date l2 = (Date) o2.get("updateTime");
            return l1.compareTo(l2);
        });
        // 把文件夹排在前面
        dataList.sort((o1, o2) -> {
            Boolean l1 = (boolean) o1.get("isDir");
            Boolean l2 = (boolean) o2.get("isDir");
            return l2.compareTo(l1);
        });

        rs.put("code", 200);
        rs.put("msg", "查询成功");
        rs.put("data", dataList);
        return rs;
    }

    @Override
    public Map<String, Object> remoteDel(String file) {
        // 找到要删除文件的真实路径
        String[] fileS = file.split("/");
        file = fileS[fileS.length - 1];
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO userVO = NodeUtil.toUser(userJson);
        List<FileVO> fileByName = fileDao.getFileByName(file, userVO.getUid());
        if (ObjUtil.isNotEmpty(fileByName)) {
            FileVO fileVO = fileByName.getFirst();
            HashMap<String, Object> param = new HashMap<>();
            param.put("file", fileVO.getParentPath() + SLASH + fileVO.getFname());
            param.put("user", userJson);
            // 存入节点信息
            param.put("who", "master");
            String realPath = fileVO.getRealPath();
            String res = HttpUtil.post(realPath + "/api/del", param);
            JSONObject resJson = JSON.parseObject(res);
            if (ObjUtil.equals(resJson.get("code"), 200)) {
                // 删除成功将容量加回节点
                String worker = CacheUtil.getKey(realPath);
                NodeUtil.updateFree(worker, fileVO.getSize());
                fileDao.deleteFileByFileName(file);
            }
            return ResUtil.getRS(Convert.toInt(resJson.get("code")), (String) resJson.get("msg"));
        }
        return ResUtil.getRS(500, "文件未找到");
    }

    @Override
    public Map<String, Object> del(String file) {
        if (file != null && !file.isEmpty()) {
            String dir = "/";
            String fileDir = system.getFileDir() + SLASH + dir;
            File f = FileUtil.file(fileDir + file);
            File smF = FileUtil.file(fileDir + "sm/" + file);
            if (f.exists()) {
                // 文件
                if (f.isFile()) {
                    FileUtil.del(f);
                    if (smF.exists() && smF.isFile()) {
                        FileUtil.del(smF);
                    }
                    return getRS(200, "文件删除成功");
                } else {
                    // 目录
                    FileService.forDelFile(f);
                    if (smF.exists() && smF.isDirectory()) {
                        FileService.forDelFile(smF);
                    }
                    return getRS(200, "目录删除成功");
                }
            } else {
                return getRS(500, "文件或目录不存在");
            }
        }
        return getRS(500, "文件或目录删除失败");
    }


    @Override
    public Map<String, Object> rename(String parentPath, String oldFile, String newFile) {
        oldFile = HtmlUtil.unescape(URLUtil.decode(oldFile));
        newFile = HtmlUtil.unescape(URLUtil.decode(newFile));
        if (oldFile.contains("/") || newFile.contains("/")
                || oldFile.contains("\"") || newFile.contains("\"")
                || oldFile.contains("'") || newFile.contains("'")) {
            return ResUtil.getRS(500, "不可有特殊字符");
        }
        if (ObjUtil.equals(oldFile, newFile)) {
            return ResUtil.getRS(500, "与原文件名相同");
        }
        Long l = fileDao.renameFileByFileName(oldFile, newFile);
        if (l > 0) {
            return ResUtil.getRS(200, "修改成功");
        }
        return ResUtil.getRS(500, "修改失败");
    }

    @Override
    public Map<String, Object> renameDir(String parentPath, String oldFile, String newFile) {
        oldFile = HtmlUtil.unescape(URLUtil.decode(oldFile));
        newFile = HtmlUtil.unescape(URLUtil.decode(newFile));
        if (oldFile.contains("/") || newFile.contains("/")
                || oldFile.contains("\"") || newFile.contains("\"")
                || oldFile.contains("'") || newFile.contains("'")) {
            return ResUtil.getRS(500, "路径不可有特殊字符");
        }
        if (ObjUtil.equals(oldFile, newFile)) {
            return ResUtil.getRS(500, "与原文件夹名相同");
        }
        Long l = fileDao.renameDirByDirName(oldFile, newFile);
        if (l > 0) {
            return ResUtil.getRS(200, "修改成功");
        }
        return ResUtil.getRS(500, "修改失败");
    }

    @Override
    public Map<String, Object> mkdir(String curPos, String dirName) {
        dirName = HtmlUtil.unescape(URLUtil.decode(dirName));
        if (dirName.contains("/") || dirName.contains("\"") || dirName.contains("\'")) {
            return ResUtil.getRS(500, "路径不可有特殊字符");
        }
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO user = NodeUtil.toUser(userJson);
        dirName = UUID.randomUUID() + dirName;
        Object mkdir = fileDao.mkdir(curPos, dirName, user.getUid());
        if (ObjUtil.isNull(mkdir)) {
            return getRS(500, "文件夹已存在");
        }
        if (ObjUtil.isNotEmpty(mkdir)) {
            return getRS(200, "创建成功");
        }
        return getRS(500, "创建失败");
    }

    @Override
    public Map<String, Object> remoteShare(String file, int time) {
        // 查找file的realPath
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO user = NodeUtil.toUser(userJson);
        List<FileVO> fileByName = fileDao.getFileByName(file, user.getUid());
        if (ObjUtil.isNotEmpty(fileByName)) {
            HashMap<String, Object> param = new HashMap<>();
            FileVO fileVO = fileByName.getFirst();
            param.put("user", userJson);
            // 存入节点信息
            param.put("who", "master");
            param.put("file", file);
            param.put("time", time);
            String reqUrl = HttpTypeEnum.HTTP.getName() + fileVO.getRealPath() + "/api/share";
            String res = HttpUtil.post(reqUrl, param);
            JSONObject jsonObject = JSON.parseObject(res);
            return ResUtil.getRS(Convert.toInt(jsonObject.get("code")), jsonObject.get("msg").toString(), jsonObject.get("url").toString());
        }
        return null;
    }

    @Override
    public Map<String, Object> share(String file, String user, int time) {
        // 若文件已经分享
        if (ObjUtil.isNotEmpty(CacheUtil.dataMap) &&
                CacheUtil.dataMap.containsValue(file)) {
            Set<String> set = CacheUtil.dataExpireMap.keySet();
            // 找出分享的key
            String key = null;
            for (String t : set) {
                if (CacheUtil.get(t) != null && CacheUtil.get(t).equals(file)) {
                    key = t;
                    break;
                }
            }
            // 是否在有效期内
            if (key != null) {
                Date expireDate = CacheUtil.dataExpireMap.get(key);
                if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                    return getRS(200, "该文件已分享", system.getDomain() + SLASH + "share?sid=" + key);
                }
            }
        }
        String sid = UUID.fastUUID().toString();
        HashMap<String, Object> shareFile = new HashMap<>();
        UserVO userVo = NodeUtil.toUser(user);
        shareFile.put("file", file);
        shareFile.put("username", userVo.getUname());
        CacheUtil.put(sid, shareFile, time);
        return getRS(200, "分享成功", system.getDomain() + SLASH + "share?sid=" + sid);
    }

    @Override
    public ModelAndView sharePage(String sid) {
        ModelAndView modelAndView = new ModelAndView();
        if (ObjUtil.isNotEmpty(CacheUtil.dataMap) &&
                CacheUtil.dataMap.containsKey(sid)) {
            // 是否在有效期内
            Date expireDate = CacheUtil.dataExpireMap.get(sid);
            if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                HashMap<String, Object> shareFile = (HashMap<String, Object>) CacheUtil.get(sid);
                // 文件是否存在
                String url = shareFile.get("file").toString();
                log.debug("分享文件:{}", url);
                File existFile = FileUtil.file(system.getFileDir() + SLASH + url);
                if (!existFile.exists()) {
                    modelAndView.put("exists", false);
                    modelAndView.put("msg", "该文件已不存在~");
                    modelAndView.view("share.html");
                    return modelAndView;
                }
                // 检测文件类型
                String contentType = null;
                String suffix = existFile.getName().substring(existFile.getName().lastIndexOf(".") + 1);
                contentType = FileTypeUtil.getType(existFile);
                // 获取文件图标、文件名、图片文件缩略图片地址、过期时间
                modelAndView.put("sid", sid);
                modelAndView.put("username", shareFile.get("username"));
                modelAndView.put("type", getFileType(suffix, contentType));
                modelAndView.put("exists", true);
                modelAndView.put("fileName", url.substring(url.lastIndexOf('/') + 1));
                // 是否有缩略图
                String smUrl = "sm/" + url;
                if (new File(system.getFileDir() + smUrl).exists()) {
                    modelAndView.put("hasSm", true);
                    // 缩略图地址
                    modelAndView.put("smUrl", "share/file/sm?sid=" + sid);
                }
                modelAndView.put("expireTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(CacheUtil.dataExpireMap.get(sid)));
                // 是否支持浏览器在线查看
                boolean flag = false;
                if (cn.lyxlz.fastfs.util.FileTypeUtil.canOnlinePreview(contentType)) {
                    flag = true;
                }
                modelAndView.put("preview", flag);
                modelAndView.view("share.html");
                return modelAndView;
            }
        }
        return modelAndView;
    }


    /**
     * 获取当前日期
     */
    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        return sdf.format(new Date());
    }


    private ModelAndView useNginx(String filePath, ModelAndView modelAndView) {
        if (system.getNginxUrl() == null) {
            system.setNginxUrl(SLASH);
        }
        if (!system.getNginxUrl().endsWith(SLASH)) {
            system.setNginxUrl(system.getNginxUrl() + SLASH);
        }
        String newName;
        try {
            newName = URLEncoder.encode(filePath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            newName = filePath;
        }
        modelAndView.view(system.getNginxUrl() + newName);
        return modelAndView;
    }

    /**
     * 获取源文件或者缩略图文件
     *
     * @param p        p
     * @param download 下载
     * @return {@link String}
     */
    private ModelAndView getFile(String p, boolean download, ModelAndView modelAndView) {
        if (system.getUseNginx()) {
            return useNginx(p, modelAndView);
        }
        outputFile(p, download, modelAndView);
        return null;
    }

    /**
     * 返回分享源文件或其缩略图页面或文件
     *
     * @param sid          sid
     * @param download     下载
     * @param modelAndView 模型和视图
     * @return {@link String}
     */
    private ModelAndView returnShareFileOrSm(String sid, boolean download, ModelAndView modelAndView) {
        String url = null;
        String username = null;
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsKey(sid)) {
                // 是否在有效期内
                Date expireDate = CacheUtil.dataExpireMap.get(sid);
                if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                    Map<String, Object> shareFile = (Map<String, Object>) CacheUtil.get(sid);
                    url = shareFile.get("file").toString();
                    username = shareFile.get("username").toString();
                    // 文件是否存在
                    File existFile = FileUtil.file(system.getFileDir() + SLASH + url);
                    if (!existFile.exists()) {
                        modelAndView.view("error.html");
                        modelAndView.put("msg", "该文件已不存在~");
                        return modelAndView;
                    }
                } else {
                    modelAndView.view("error.html");
                    modelAndView.put("msg", "分享文件已过期");
                    return modelAndView;
                }
            } else {
                modelAndView.view("error.html");
                modelAndView.put("msg", "无效的sid");
                return modelAndView;
            }
        }
        // 为了和下载文件的请求参数统一
        url = username + "/" + url;
        return getFile(url, download, modelAndView);
    }

    /**
     * 输出文件流
     *
     * @param file         文件
     * @param download     下载
     * @param modelAndView
     */
    private ModelAndView outputFile(String file, boolean download, ModelAndView modelAndView) {
        // 判断文件是否存在
        String uname = file.split("/")[0];
        List<UserVO> user = userDao.getUser(uname);
        List<FileVO> fileByName = fileDao.getFileByName(file.replace(uname + "/", ""), user.getFirst().getUid());
        if (ObjUtil.isNotEmpty(fileByName)) {
            file = fileByName.getFirst().getParentPath() + SLASH + fileByName.getFirst().getFname();
        }
        log.debug("下载文件: {}", system.getFileDir() + file);
        File inFile = FileUtil.file(system.getFileDir() + file);
        // 文件不存在
        if (!inFile.exists()) {
            modelAndView.put("Content-Type", "text/html;charset=UTF-8");
            modelAndView.view("404.html");
            return modelAndView;
        }
        // 获取文件类型
        String contentType = null;
        contentType = FileTypeUtil.getType(inFile);
        // 图片、文本文件,则在线查看
        log.info("文件类型：" + contentType);
        if (cn.lyxlz.fastfs.util.FileTypeUtil.canOnlinePreview(contentType) && !download) {
            modelAndView.put("Content-Type", contentType);
            modelAndView.put("Character-Encoding", "UTF-8");
        } else {
            // 其他文件,强制下载
            modelAndView.put("Content-Type", "application/force-download");
            String newName;
            newName = URLEncoder.encode(inFile.getName(), StandardCharsets.UTF_8);
            modelAndView.put("Content-Disposition", ("attachment;fileName=" + newName));
        }
        // 输出文件流
        try {
            Context.current().outputAsFile(new DownloadedFile(
                    contentType,
                    new FileInputStream(inFile),
                    file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return modelAndView;
    }

    /**
     * 获取文件类型
     *
     * @param suffix      后缀
     * @param contentType 内容类型
     * @return {@link String}
     */
    private String getFileType(String suffix, String contentType) {
        String type;
        if (FileTypeEnum.PPT.getName().equalsIgnoreCase(suffix) || FileTypeEnum.PPTX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PPT.getName();
        } else if (FileTypeEnum.DOC.getName().equalsIgnoreCase(suffix) || FileTypeEnum.DOCX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.DOC.getName();
        } else if (FileTypeEnum.XLS.getName().equalsIgnoreCase(suffix) || FileTypeEnum.XLSX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.XLS.getName();
        } else if (FileTypeEnum.PDF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PDF.getName();
        } else if (FileTypeEnum.HTML.getName().equalsIgnoreCase(suffix) || FileTypeEnum.HTM.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.HTM.getName();
        } else if (FileTypeEnum.TXT.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.TXT.getName();
        } else if (FileTypeEnum.SWF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.FLASH.getName();
        } else if (FileTypeEnum.ZIP.getName().equalsIgnoreCase(suffix) || FileTypeEnum.RAR.getName().equalsIgnoreCase(suffix) || FileTypeEnum.SEVENZ.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.ZIP.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.AUDIO.getName() + SLASH)) {
            type = FileTypeEnum.MP3.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.VIDEO.getName() + SLASH)) {
            type = FileTypeEnum.MP4.getName();
        } else {
            type = FileTypeEnum.FILE.getName();
        }
        return type;
    }
}
