package features;

import cn.hutool.http.HttpUtil;
import cn.lyxlz.fastfs.App;
import cn.lyxlz.fastfs.entity.HeapNode;
import cn.lyxlz.fastfs.entity.Node;
import cn.lyxlz.fastfs.util.NodeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.snack.ONode;
import org.noear.snack.to.Toer;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RunWith(SolonJUnit4ClassRunner.class)
@SolonTest(App.class)
@Slf4j
public class HeapTest {

    @Test
    public void test() {
        Node val = new Node()
                .setNodeName("self")
                .setFree(100L)
                .setTotal(200L);
        HeapNode heapNode = new HeapNode(val);
        Node val1 = new Node()
                .setNodeName("node1")
                .setFree(120L)
                .setTotal(200L);
        Node val2 = new Node()
                .setNodeName("node2")
                .setFree(30L)
                .setTotal(200L);
        heapNode.insert(val1);
        heapNode.insert(val2);
        heapNode.deleteHeapTop();
        val1.setFree(110L);
        heapNode.insert(val1);
        log.debug("{}", heapNode.size());
        log.debug("测试通过");
    }

    @Test
    public void test2() {
        String res = HttpUtil.get("http://172.31.227.99:8081/info", StandardCharsets.UTF_8);
        String nodeJson = JSON.parseObject(res).get("msg").toString();
//        Node node = JSON.parseObject(nodeJson, Node.class);
        Node node = NodeUtil.toNode(nodeJson);
        System.out.println(node);
    }

    @Inject Node node;
    @Test
    public void test3() {
        String json = NodeUtil.toJson(node);
        Node node1 = NodeUtil.toNode(json);
        System.out.println(node1);
    }
}
