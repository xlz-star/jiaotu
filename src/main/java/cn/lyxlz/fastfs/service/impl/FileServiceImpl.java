package cn.lyxlz.fastfs.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.lyxlz.fastfs.constant.FileTypeEnum;
import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.service.FileService;
import cn.lyxlz.fastfs.util.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.extend.sqltoy.annotation.Db;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Override
    public Map<String, Object> upload(UploadedFile file, String curPos) {
        curPos = curPos.substring(1) + SLASH;
        checkFileDir();
        String dir = curPos;
        String username = (String) StpUtil.getLoginId();
        // 判断是否是管理员用户，管理员可以访问其他用户文件夹
        String fileDir = ObjUtil.equals(username, system.getUname()) ? system.getFileDir() + SLASH + dir : system.getFileDir() + SLASH + username + SLASH + dir;
        // 文件原始名称
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String prefix = fileName.substring(0, fileName.lastIndexOf("."));
        // 保存到磁盘
        File outFile;
        String path;
        if (ObjUtil.isNotEmpty(system.getUuidName()) && system.getUuidName()) {
            path = curPos + UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix;
            outFile = FileUtil.file(fileDir + path);
        } else {
            int index = 1;
            path = curPos + fileName;
            outFile = FileUtil.file(fileDir + fileName);
            // 防止文件名重复，以数字后缀重命名
            while (outFile.exists()) {
                path = curPos + prefix + "(" + index + ")." + suffix;
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
            Map<String, Object> rs = getRS(200, "上传成功", path);
            // 生成缩略图
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
        checkFileDir();
        HashMap<String, Object> rs = new HashMap<>();
        if (ObjUtil.isEmpty(dir) || SLASH.equals(dir)) {
            dir = "";
        } else if (dir.startsWith(SLASH)) {
            dir = dir.substring(1);
        }
        String username = (String) StpUtil.getLoginId();
        // 判断是否是管理员用户，管理员可以访问其他用户文件夹
        String path = ObjUtil.equals(username, system.getUname()) ? system.getFileDir() + SLASH + dir : system.getFileDir() + SLASH + username + SLASH + dir;
        File file = FileUtil.file(path);
        File[] listFiles = file.listFiles();
        ArrayList<Map<String, Object>> dataList = new ArrayList<>();
        if (ObjUtil.isNotEmpty(listFiles)) {
            for (File f : listFiles) {
                if ("sm".equals(f.getName())) {
                    continue;
                }
                HashMap<String, Object> m = new HashMap<>();
                // 文件名称
                m.put("name", f.getName());
                // 修改时间
                m.put("updateTime", f.lastModified());
                // 是否是目录
                m.put("isDir", f.isDirectory());
                if (f.isDirectory()) {
                    // 文件类型
                    m.put("type", "dir");
                } else {
                    boolean flag = false;
                    if (cn.lyxlz.fastfs.util.FileTypeUtil.canOnlinePreview(FileTypeUtil.getType(f))) {
                        flag = true;
                    }
                    m.put("preview", flag);
                    // 文件地址
                    m.put("url", (dir.isEmpty() ? dir : dir + SLASH) + f.getName());
                    // 获取文件类型
                    String contentType = null;
                    String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    contentType = FileTypeUtil.getType(f);
                    if (accept != null && !accept.trim().isEmpty() && !accept.equals("file")) {
                        if (contentType == null || !contentType.startsWith(accept + SLASH)) {
                            continue;
                        }
                        if (ObjUtil.isNotEmpty(mExts)) {
                            for (String ext : mExts) {
                                if (!f.getName().endsWith("." + ext)) {
                                    continue;
                                }
                            }
                        }
                    }
                    // 获取文件图标
                    m.put("type", getFileType(suffix, contentType));
                    // 是否有缩略图
                    String smUrl = "sm/" + (dir.isEmpty() ? dir : (dir + SLASH)) + f.getName();
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
            Long l1 = (long) o1.get("updateTime");
            Long l2 = (long) o2.get("updateTime");
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
    public Map<String, Object> del(String file) {
        checkFileDir();
        if (file != null && !file.isEmpty()) {
            String username = (String) StpUtil.getLoginId();
            String dir = "/";
            // 判断是否是管理员用户，管理员可以访问其他用户文件夹
            String fileDir = ObjUtil.equals(username, system.getUname()) ? system.getFileDir() + SLASH + dir : system.getFileDir() + SLASH + username + SLASH + dir;
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
    public Map<String, Object> rename(String oldFile, String newFile) {
        checkFileDir();
        String username = (String) StpUtil.getLoginId();
        String dir = "/";
        String fileDir = system.getFileDir() + username + SLASH + dir;
        if (StrUtil.isNotEmpty(oldFile) && StrUtil.isNotEmpty(newFile)) {
            // 原文件
            File f = FileUtil.file(fileDir + oldFile);
            // 原文件缩略图
            File smF = FileUtil.file(fileDir + "sm/" + oldFile);
            // 新文件
            File nFile = FileUtil.rename(f, fileDir + newFile, false);
            if (nFile.exists()) {
                if (smF.exists()) {
                    // 新文件缩略图
                    FileUtil.rename(smF, fileDir + "sm/" + newFile, false);
                }
                return getRS(200, "重命名成功", SLASH + newFile);
            }
        }
        return getRS(500, "重命名失败");
    }

    @Override
    public Map<String, Object> mkdir(String curPos, String dirName) {
        checkFileDir();
        if (StrUtil.isNotEmpty(curPos) && StrUtil.isNotEmpty(dirName)) {
            curPos = curPos.substring(1);
//            String dirPath = system.getFileDir() + curPos + SLASH + dirName;
            String username = (String) StpUtil.getLoginId();
            // 判断是否是管理员用户，管理员可以访问其他用户文件夹
            String dirPath = ObjUtil.equals(username, system.getUname()) ? system.getFileDir() + SLASH + curPos + SLASH + dirName : system.getFileDir() + SLASH + username + SLASH + dirName;

            File f = FileUtil.file(dirPath);
            if (f.exists()) {
                return getRS(500, "目录已存在");
            }
            if (FileUtil.mkdir(dirPath).exists()) {
                return getRS(200, "创建成功");
            }
        }
        return getRS(500, "创建失败");
    }

    @Override
    public Map<String, Object> share(String file, int time) {
        String username = (String) StpUtil.getLoginId();
        // 判断是否是管理员用户，管理员可以访问其他用户文件夹
        String dir = "/";
//        file = system.getFileDir() + username + SLASH + dir + SLASH + file;
        file = ObjUtil.equals(username, system.getUname()) ? dir + SLASH + file : username + SLASH + file;
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
        checkFileDir();
        String sid = java.util.UUID.randomUUID().toString();
        CacheUtil.put(sid, file, time);
        return getRS(200, "分享成功", system.getDomain() + SLASH + "share?sid=" + sid);
    }

    @Override
    public ModelAndView sharePage(String sid) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.put("username", (String) StpUtil.getLoginId());
        if (ObjUtil.isNotEmpty(CacheUtil.dataMap) &&
                CacheUtil.dataMap.containsKey(sid)) {
            // 是否在有效期内
            Date expireDate = CacheUtil.dataExpireMap.get(sid);
            if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                String url = (String) CacheUtil.get(sid);
                // 文件是否存在
                File existFile = new File(system.getFileDir() + SLASH + url);
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
     * 检查文件目录属性是否正确
     */
    private void checkFileDir() {
        if (system.getFileDir() == null) {
            system.setFileDir(SLASH);
        }
        if (!system.getFileDir().endsWith(SLASH)) {
            system.setFileDir(system.getFileDir() + SLASH);
        }
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
        checkFileDir();
        if (p.startsWith(system.getUname())) {
            p = p.replaceFirst(system.getUname(), "");
        }
        outputFile(system.getFileDir() + p, download, modelAndView);
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
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsKey(sid)) {
                // 是否在有效期内
                Date expireDate = CacheUtil.dataExpireMap.get(sid);
                if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                    url = (String) CacheUtil.get(sid);
                    // 文件是否存在
                    File existFile = new File(system.getFileDir() + SLASH + url);
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
        File inFile = FileUtil.file(file);
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
