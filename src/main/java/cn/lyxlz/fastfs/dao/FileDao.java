package cn.lyxlz.fastfs.dao;

import cn.lyxlz.fastfs.entity.FileVO;

import java.util.List;

/**
 * 数据库文件操作
 */
public interface FileDao {
    /**
     * 获取文件真实路径
     *
     * @param fileName
     * @param uid
     * @return
     */
    List<FileVO> getFileRealPath(String fileName, String uid);

    /**
     * 通过文件名获取文件信息
     *
     * @param fileName
     * @return
     */
    List<FileVO> getFileByName(String fileName, String uid);

    /**
     * 通过文件名获取文件信息
     *
     * @param dirName
     * @param uid
     * @return
     */
    List<FileVO> getDirByName(String dirName, String uid);

    /**
     * 通过用户id获取文件信息
     *
     * @param uid
     * @return
     */
    List<FileVO> getFileByUserId(String parentPath, String uid);

    /**
     * 为管理员提供查询所有文件接口
     *
     * @return
     */
    List<FileVO> getFiles(String parentPath, String uid);

    /**
     * 保存文件信息
     * Params: file
     */
    Object saveFile(FileVO file);

    /**
     * 创建文件夹
     *
     * @param parentPath
     * @param dirName
     * @param uid
     * @return
     */
    Object mkdir(String parentPath, String dirName, String uid);

    /**
     * 通过文件名删除文件
     *
     * @param fname
     * @return
     */
    Long deleteFileByFileName(String fname);

    /**
     * 通过文件夹名删除
     *
     * @param dirName
     * @return
     */
    Long deleteDirByDirName(String dirName);

    /**
     * 文件重命名
     *
     * @param oldName
     * @param newName
     * @return
     */
    Long renameFileByFileName(String oldName, String newName);


    /**
     * 文件夹重命名
     *
     * @param oldName 旧名称
     * @param newName 新名称
     * @return {@link Long}
     */
    Long renameDirByDirName(String oldName, String newName);
}
