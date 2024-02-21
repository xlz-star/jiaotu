package cn.lyxlz.fastfs.config;

import cn.lyxlz.fastfs.entity.Node;
import cn.lyxlz.fastfs.util.NodeUtil;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Configuration
public class NodeConfig {

    @Bean
    public Node getNode(
            @Inject("${fs.node.nodeName}") String nodeName,
            @Inject("${fs.node.total}") String total
    ) {
        Long t = NodeUtil.totalStr2Long(total);
        return new Node().setNodeName(nodeName)
                .setTotal(t)
                .setFree(t);
    }

}
