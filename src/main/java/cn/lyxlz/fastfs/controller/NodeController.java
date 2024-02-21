package cn.lyxlz.fastfs.controller;

import cn.lyxlz.fastfs.annotation.Login;
import cn.lyxlz.fastfs.entity.Node;
import cn.lyxlz.fastfs.entity.System;
import cn.lyxlz.fastfs.service.DistributService;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import cn.lyxlz.fastfs.util.ResUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import lombok.extern.slf4j.Slf4j;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class NodeController {

    @Inject
    System system;

    @Inject
    Node node;

    @Inject
    DistributService distributService;

    /**
     * 返回节点信息
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    @Get
    @Mapping("/info")
    public Map<String, Object> getNodeInfo() {
        // 只有从节点才需要返回信息
        if (!system.getMaster()) {
            String nodeJson = NodeUtil.toJson(node);
            log.debug("当前节点信息{}", nodeJson);
            return ResUtil.getRS(200, nodeJson);
        }
        return ResUtil.getRS(403, "不能访问主节点信息");
    }

    @Get
    @Mapping("/worklist")
    @Login
    public Map<String, Object> getNodeList() {
        // 只有主节点才需要查看工作节点列表
        if (system.getMaster()) {
            // 先更新一下主机列表
            distributService.findNode();
            // 查看信息
            List<Node> nodes = NodeUtil.getNodes();
            String load = NodeUtil.toJson(nodes);
            return ResUtil.getRS(200, load);
        }
        return ResUtil.getRS(403, "不能访问主节点信息");
    }
}
