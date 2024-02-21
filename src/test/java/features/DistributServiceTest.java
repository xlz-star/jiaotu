package features;

import cn.hutool.core.io.FileUtil;
import cn.lyxlz.fastfs.App;
import cn.lyxlz.fastfs.entity.UserVO;
import cn.lyxlz.fastfs.service.DistributService;
import cn.lyxlz.fastfs.util.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import java.io.FileNotFoundException;

@RunWith(value = SolonJUnit4ClassRunner.class)
@SolonTest(App.class)
@Slf4j
public class DistributServiceTest {

    @Inject
    DistributService distributService;

    @Test
    public void test() throws FileNotFoundException {
        UserVO user = new UserVO()
                .setUname("admin")
                .setPasswd("123");
//        StpUtil.login(user.getUname());
        CacheUtil.put("user", user, Integer.MAX_VALUE);
        distributService.findNode();
        String res = distributService.sendResource(FileUtil.file(),
                "/");
        log.debug(res);
    }

}
