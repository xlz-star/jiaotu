package cn.lyxlz.fastfs.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.lyxlz.fastfs.entity.HeapNode;
import cn.lyxlz.fastfs.entity.Node;
import cn.lyxlz.fastfs.entity.UserVO;
import com.alibaba.fastjson2.JSON;

import java.util.ArrayList;
import java.util.List;

import static cn.hutool.crypto.Mode.CBC;
import static cn.hutool.crypto.Padding.ZeroPadding;

/**
 * 节点工具
 *
 * @author xlz
 * @date 2024/01/13
 */
public class NodeUtil {
    private static final String KEY = "lyxlz41199999999";
    private static final String IV = "iviviviviviviviv";

    private static final SM4 SM4 = new SM4(CBC, ZeroPadding, KEY.getBytes(CharsetUtil.CHARSET_UTF_8), IV.getBytes(CharsetUtil.CHARSET_UTF_8));

    private static HeapNode nodes = new HeapNode(
            new Node().setNodeName("self").setTotal(0L).setFree(0L)
    );


    /**
     * 堆中节点个数
     *
     * @return
     */
    public synchronized static int size() {
        return nodes.size();
    }

    /**
     * 插入
     *
     * @param num 节点的值
     */
    public synchronized static void insert(Node num) {
        nodes.insert(num);
    }

    /**
     * 是否包含节点
     *
     * @param node
     * @return boolean
     */
    public synchronized static boolean containsNode(Node node) {
        return nodes.containsNode(node);
    }

    /**
     * 获取堆顶节点
     *
     * @return
     */
    public static synchronized Node getHeapTop() {
        return nodes.getHeapTop();
    }

    /**
     * 调整堆
     */
    public static synchronized void heapify() {
        nodes.heapify();
    }

    /**
     * 统计剩余容量
     * @return
     */
    public synchronized static long sumFree() {
        return nodes.sumFree();
    }

    /**
     * 更新节点剩余容量（加）
     *
     * @param nodeName 节点名
     * @param free     新的容量
     */
    public static synchronized void updateFree(String nodeName, Long free) {
        List<Node> nodeList = getNodes();
        nodeList.forEach(f -> {
            if (ObjUtil.equals(f.getNodeName(), nodeName)) {
                f.setFree(f.getFree() + free);
                nodes.heapify();
            }
        });
    }

    /**
     * 获取所有节点
     */
    public synchronized static List<Node> getNodes() {
        ArrayList<Node> nodes = new ArrayList<>();
        inorderTraversal(NodeUtil.nodes, nodes);
        return nodes;
    }

    /**
     * 中序遍历返回堆的节点值
     *
     * @param node 节点
     */
    private synchronized static void inorderTraversal(HeapNode node, List<Node> nodes) {
        if (node == null) {
            return;
        }
        nodes.add(node.val);
        inorderTraversal(node.left, nodes);
        inorderTraversal(node.right, nodes);
    }

    /**
     * 将容量字符串转为Double类型
     *
     * @param total 总容量
     * @return {@link Double}
     */
    public static Long totalStr2Long(String total) {
        if (ObjUtil.isEmpty(total)) {
            throw new RuntimeException("转换字符串为空");
        }
        Long totalLong = Convert.toLong(ReUtil.get("(\\d+)(\\D)", total, 1)); // 容量数字
        String totalUnit = ReUtil.get("(\\d+)(\\D)", total, 2); // 容量单位
        switch (totalUnit) {
            case "M" -> {
                return totalLong * 1024;
            }
            case "G" -> {
                return totalLong * 1024 * 1024;
            }
            default -> throw new RuntimeException("仅支持单位：\"M\"、\"G\"");
        }

    }

    /**
     * 将节点数据转成加密的JSON数据
     *
     * @param node 节点
     * @return {@link String}
     */
    public static String toJson(Node node) {
        // 转成Json格式
        String nodeJson = JSON.toJSONString(node);
        // 加密
        return SM4.encryptHex(nodeJson);
    }

    /**
     * 将节点数组转成加密的JSON数据
     *
     * @param nodes 节点数组
     * @return {@link String}
     */
    public static String toJson(List<Node> nodes) {
        // 转成Json格式
        return JSON.toJSONString(nodes);
    }

    /**
     * 将用户数据转成加密的JSON数据
     *
     * @param user 用户
     * @return {@link String}
     */
    public static String toJson(UserVO user) {
        String userJson = JSON.toJSONString(user);
        return SM4.encryptHex(userJson);
    }

    /**
     * 从JSON数据转为Node对象
     *
     * @param json JSON数据
     * @return {@link Node}
     */
    public static Node toNode(String json) {
        // 解密
        String nodeJson = SM4.decryptStr(json, CharsetUtil.CHARSET_UTF_8);
        return JSON.parseObject(nodeJson, Node.class);
    }

    /**
     * 从JSON数据转为User对象
     *
     * @param json JSON数据
     * @return {@link UserVO}
     */
    public static UserVO toUser(String json) {
        String userJson = SM4.decryptStr(json, CharsetUtil.CHARSET_UTF_8);
        return JSON.parseObject(userJson, UserVO.class);
    }


}
