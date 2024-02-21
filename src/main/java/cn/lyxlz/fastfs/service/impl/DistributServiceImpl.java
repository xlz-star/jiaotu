package cn.lyxlz.fastfs.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpUtil;
import cn.lyxlz.fastfs.constant.HttpTypeEnum;
import cn.lyxlz.fastfs.dao.FileDao;
import cn.lyxlz.fastfs.entity.FileVO;
import cn.lyxlz.fastfs.entity.Node;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.entity.UserVO;
import cn.lyxlz.fastfs.service.DistributService;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import cn.lyxlz.fastfs.util.ResUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.io.File;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 分布式服务实现
 *
 * @author xlz
 * @date 2024/01/13
 */
@Component
@Slf4j
public class DistributServiceImpl implements DistributService {

    @Inject
    Node node;

    @Inject
    System system;

    @Inject
    FileDao fileDao;

    /**
     * 节点发现
     */
    @Override
    public void findNode() {
        // 只有主节点需要进行主机发现
        List<String> works = system.getWorks();
        for (String work : works) {
            String workerUrl = HttpTypeEnum.HTTP.getName() + work + "/info";
            String res = HttpUtil.get(workerUrl);
            // 解析json，获取msg字段
            String nodeJson = JSON.parseObject(res).get("msg").toString();
            Node node = NodeUtil.toNode(nodeJson);
            // 查看节点是否已保存
            boolean containsNode = NodeUtil.containsNode(node);
            if (containsNode) continue;
            // 将从节点信息保存在堆中
            NodeUtil.insert(node);
            // 将节点信息保存到缓存中（设置缓存七天）
            CacheUtil.put(node.getNodeName(), work, 10080);
        }
    }

    /**
     * 资源转发
     *
     * @param local   文件
     * @param curPos 当前位置
     * @return
     */
    @Override
    public String sendResource(File local, String curPos) {
        // 更新节点列表
        findNode();
        // 获取集群中容量最大的节点
        Node heapTop = NodeUtil.getHeapTop();
        Long topFree = heapTop.getFree();
        // 判断容量是否大于上传文件的大小
        if (local.length() < topFree) {
            String workUrl = (String) CacheUtil.get(heapTop.getNodeName());
            // 将资源转发到对应节点的上传接口
            String reqUrl = HttpTypeEnum.HTTP.getName() + workUrl + "/file/recv";
            Map<String, Object> param = new HashMap<>();
            param.put("file", local);
            param.put("curPos", curPos);
            // 动态获取登录用户
            String string = StpUtil.getLoginId().toString();
            // 存入用户信息
            String userJson = CacheUtil.get(string).toString();
            param.put("user", userJson);
            // 存入节点信息
            param.put("who", "master");
            UserVO loginUser = NodeUtil.toUser(userJson);
            String res = HttpUtil.post(reqUrl, param);
            JSONObject jsonObject = JSON.parseObject(res);
            if (ObjUtil.equals(jsonObject.get("code"), 200)) {
                // 将文件实际路径写入数据库
                String name = local.getName();
                FileVO fileVO = new FileVO()
                        .setFid(UUID.fastUUID().toString())
                        .setFname(name)
                        .setRealPath(workUrl)
                        .setViewPath(name.substring(32))
                        .setParentPath(curPos)
                        .setUid(loginUser.getUid())
                        .setUpdateTime(new Date(new java.util.Date().getTime()))
                        .setFtype(FileTypeUtil.getType(local))
                        .setSize(local.length() / 1024)
                        .setDir(FileUtil.isDirectory(local));
                fileDao.saveFile(fileVO);
                // 节点更新容量
                heapTop.setFree(topFree - local.length());
                NodeUtil.heapify();
                return res;
            }

        }
        return JSON.toJSON(ResUtil.getRS(500, "上传失败")).toString();
    }
}
