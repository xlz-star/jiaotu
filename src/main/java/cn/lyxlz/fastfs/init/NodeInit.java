package cn.lyxlz.fastfs.init;

import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.entity.FileVO;
import cn.lyxlz.fastfs.entity.Node;
import cn.lyxlz.fastfs.service.DistributService;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.util.List;

@Component
@Slf4j
public class NodeInit implements EventListener<AppLoadEndEvent> {

    @Inject
    DistributService distributService;

    @Db
    SqlToyLazyDao dao;

    @Override
    public void onEvent(AppLoadEndEvent appLoadEndEvent) throws Throwable {
        log.debug("-----------开始初始化节点信息-----------");
        distributService.findNode();
        List<FileVO> fileVOS = dao.findEntity(FileVO.class, EntityQuery.create()
                .where("ftype != 'dir'"));
        List<Node> nodes = NodeUtil.getNodes();
        if (ObjUtil.isNotEmpty(fileVOS)) {
            fileVOS.forEach(f -> {
                String realPath = f.getRealPath();
                String worker = CacheUtil.getKey(realPath);
                if (ObjUtil.isNotEmpty(nodes)) {
                    nodes.parallelStream().forEach(node -> {
                        if (ObjUtil.equals(node.getNodeName(), worker)) {
                            node.setFree(node.getFree() - f.getSize());
                        }
                    });
                }
            });
        }
        log.debug("-----------初始化节点信息完成-----------");
    }
}
