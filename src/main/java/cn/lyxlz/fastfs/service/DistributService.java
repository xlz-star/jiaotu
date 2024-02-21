package cn.lyxlz.fastfs.service;

import org.noear.solon.core.handle.UploadedFile;

import java.io.File;

/**
 * 分布式服务
 *
 * @author xlz
 * @date 2024/01/13
 */
public interface DistributService {
    /**
     * 主机发现
     *
     * @param nodes 节点
     */
    void findNode();

    /**
     * 发送资源
     *
     * @param file   文件
     * @param curPos 当前位置
     * @return
     */
    String sendResource(File local, String curPos);

}
