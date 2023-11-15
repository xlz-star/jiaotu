package cn.lyxlz.fastfs.service;

import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

/**
 * 文件服务
 *
 * @author xlz
 * @date 2023/09/02
 */
public interface FileService {
    /**
     * 根据不同系统动态获取系统路径分隔符
     */
    public static String SLASH = String.valueOf(File.separatorChar);

    /**
     * 文件上传
     *
     * @param file   文件
     * @param curPos 上传文件时所处的目录位置
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    Map<String, Object> upload(UploadedFile file, String curPos);

    /**
     * 查看/下载源文件
     *
     * @param p            文件全路径
     * @param d            是否下载,1-下载
     * @return {@link ModelAndView}
     */
    ModelAndView file(String p, int d);

    /**
     * 查看/下载分享的源文件
     *
     * @param sid          分享sid
     * @param d            d
     * @return {@link ModelAndView}
     */
    ModelAndView shareFile(String sid, int d);

    /**
     * 分享源文件的缩略图
     *
     * @param sid          sid
     * @return {@link ModelAndView}
     */
    ModelAndView shareFileSm(String sid);

    /**
     * 查看缩略图
     *
     * @param p            文件全名
     * @return {@link ModelAndView}
     */
    ModelAndView fileSm(String p);

    /**
     * 获取全部文件
     *
     * @param dir    目录
     * @param accept 接受
     * @param exts   exts
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    Map<String, Object> list(String dir, String accept, String exts) throws SQLException;

    /**
     * 递归删除目录下的文件以及目录
     *
     * @param file 文件
     */
    static void forDelFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                forDelFile(f);
            }
        }
        file.delete();
    }

    /**
     * 删除
     *
     * @param file 文件
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    Map<String, Object> del(String file);

    /**
     * 重命名
     *
     * @param oldFile 旧文件
     * @param newFile 新文件
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    Map<String, Object> rename(String oldFile, String newFile);

    /**
     * 新建文件夹
     *
     * @param curPos  过帐
     * @param dirName %1（%2）
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    Map<String, Object> mkdir(String curPos, String dirName);

    /**
     * 分享文件
     *
     * @param file 文件
     * @param time 有效时间(分钟)
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    Map<String, Object> share(String file, int time);

    /**
     * 分享文件展示页面
     *
     * @param sid          分享文件sid
     * @return {@link ModelAndView}
     */
    ModelAndView sharePage(String sid);
}
