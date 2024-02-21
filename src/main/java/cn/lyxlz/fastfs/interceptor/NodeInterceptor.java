package cn.lyxlz.fastfs.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.fastfs.dao.UserDao;
import cn.lyxlz.fastfs.entity.UserVO;
import cn.lyxlz.fastfs.util.CacheUtil;
import cn.lyxlz.fastfs.util.NodeUtil;
import cn.lyxlz.fastfs.util.ResUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;

import java.util.List;

/**
 * 节点拦截器
 *
 * @author xlz
 * @date 2024/02/17
 */
@Component
public class NodeInterceptor implements RouterInterceptor {

    @Inject
    UserDao userDao;

    @Override
    public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        // 如果是主节点访问则放行
        String userJson = ctx.paramOrDefault("user", null);
        if (ObjUtil.isNotEmpty(userJson)) {
            UserVO userVO = NodeUtil.toUser(userJson);
            // TODO 从数据库中检测用户信息是否正确
            if ("master".equals(ctx.param("who"))) {
                List<UserVO> checkUser = userDao.getUser(userVO);
                if (ObjUtil.isEmpty(checkUser)) {
                    ctx.render(ResUtil.getRS(404, "用户校验失败"));
                }
                // 登录信息缓存七天
                StpUtil.login(userVO.getUname());
                CacheUtil.put(userVO.getUname(), userJson, 10080);
            }
        }
        chain.doIntercept(ctx, mainHandler);
    }
}
