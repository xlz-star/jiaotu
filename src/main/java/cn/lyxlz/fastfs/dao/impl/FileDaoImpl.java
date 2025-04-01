package cn.lyxlz.fastfs.dao.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.dao.FileDao;
import cn.lyxlz.fastfs.entity.FileVO;
import cn.lyxlz.fastfs.entity.UserVO;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.lyxlz.fastfs.service.FileService.SLASH;

@Component
public class FileDaoImpl implements FileDao {
    @Db
    LightDao dao;

    @Override
    public List<FileVO> getFileByLike(String fileName, String uid) {
        return dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[fname like ?] #[ and uid = ?]")
                        .values(fileName, uid));
    }

    @Override
    public List<FileVO> getFileRealPath(String fileName, String uid) {
        return dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[fname = ?] #[ and uid = ?]")
                        .values(fileName, uid));
    }

    @Override
    public List<FileVO> getFileByName(String fileName, String uid) {
        return dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[fname = ?] #[ and uid = ?]")
                        .values(fileName, uid));
    }

    @Override
    public List<FileVO> getDirByName(String dirName, String uid) {
        return dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[parent_path = ?] #[ and uid = ?] #[and ftype = 'dir']")
                        .values(dirName, uid));
    }

    @Override
    public List<FileVO> getFileByUserId(String parentPath, String uid) {
        return dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[parent_path = ?] #[ and uid = ?]")
                        .values(parentPath, uid));
    }

    @Override
    public List<FileVO> getFiles(String parentPath, String uid) {
        return dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[parent_path = ?] #[ and uid = ?]")
                        .values(parentPath, uid));
    }

    @Override
    @Tran
    public Object saveFile(FileVO file) {
        return dao.save(file);
    }

    @Override
    @Tran
    public Object mkdir(String parentPath, String dirName, String uid) {
        // 查询目录是否已存在
        List<FileVO> fileVOS = dao.findEntity(FileVO.class, EntityQuery.create()
                .where("#[view_path = ?] #[and uid = ?]")
                .values(dirName.substring(36), uid));
        AtomicBoolean flag = new AtomicBoolean(false);
        if (ObjUtil.isNotEmpty(fileVOS)) {
            fileVOS.parallelStream().forEach((f) -> {
                if (ObjUtil.equals(f.getParentPath(), parentPath)) {
                    flag.set(true);
                }
            });
            if (flag.get()) {
                return null;
            }
        }
        FileVO dir = new FileVO()
                .setFid(UUID.fastUUID().toString())
                .setFname(dirName)
                .setRealPath("")
                .setViewPath(dirName.substring(36))
                .setParentPath(parentPath)
                .setUpdateTime(new Date(new java.util.Date().getTime()))
                .setDir(true)
                .setFtype("dir")
                .setUid(uid)
                .setSize(0L);
        return dao.save(dir);
    }

    @Override
    @Tran
    public Long deleteFileByFileName(String fname) {
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO user = NodeUtil.toUser(userJson);
        return dao.deleteByQuery(FileVO.class,
                EntityQuery.create()
                        .where("#[fname = ?] #[ and uid = ?]")
                        .values(fname, user.getUid()));
    }

    @Override
    public Long deleteDirByDirName(String dirName) {
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO user = NodeUtil.toUser(userJson);
        return dao.deleteByQuery(FileVO.class,
                EntityQuery.create().showSql(true)
                        .where("#[fname = ?] #[ and uid = ?] #[ and ftype = 'dir']")
                        .values(dirName, user.getUid()));
    }


    @Override
    @Tran
    public Long renameFileByFileName(String oldName, String newName) {
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO user = NodeUtil.toUser(userJson);
        List<FileVO> fileVOS = dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[fname = ?] #[ and uid = ?]")
                        .values(oldName, user.getUid()));
        if (ObjUtil.isNotEmpty(fileVOS)) {
            FileVO file = fileVOS.getFirst();
            file.setViewPath(newName);
            dao.update(file);
            return 1L;
        }
        return 0L;
    }

    @Override
    @Tran
    public Long renameDirByDirName(String oldName, String newName) {
        String userJson = CacheUtil.get(StpUtil.getLoginId().toString()).toString();
        UserVO user = NodeUtil.toUser(userJson);
        List<FileVO> fileVOS = dao.findEntity(FileVO.class,
                EntityQuery.create()
                        .where("#[view_path = ?] #[ and uid = ?]")
                        .values(oldName, user.getUid()));
        if (ObjUtil.isNotEmpty(fileVOS)) {
            FileVO file = fileVOS.getFirst();
            file.setViewPath(newName);
            dao.update(file);
            // 修改父文件夹
            List<FileVO> waitUpdate = dao.findEntity(FileVO.class,
                    EntityQuery.create()
                            .where("#[parent_path = ?]")
                            .values(SLASH + oldName));
            if (ObjUtil.isNotEmpty(waitUpdate)) {
                String finalNewName = SLASH + newName;
                waitUpdate.parallelStream()
                        .forEach((f) -> f.setParentPath(finalNewName));
                dao.updateAll(waitUpdate);
            }
            return 1L;
        }
        return 0L;
    }
}
