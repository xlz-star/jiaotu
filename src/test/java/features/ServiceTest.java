package features;

import cn.hutool.core.io.FileUtil;
import cn.lyxlz.fastfs.App;

import cn.lyxlz.fastfs.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * 服务测试
 *
 * @author xlz
 * @date 2023/09/09
 */
/*@RunWith(SolonJUnit4ClassRunner.class)
@SolonTest(App.class)
@Slf4j*/
public class ServiceTest extends HttpTester {
//
//    @Inject
//    FileService fileService;
//
//
//    /**
//     * mkdir测试
//     */
//    @Test
//    @Order(6)
//    public void testMkdir() {
//        FileService.forDelFile(new File("D:\\EasyFS"));
//        Map<String, Object> mkdir = fileService.mkdir("/", "test");
//        log.debug("Mkdir : {}", mkdir.get("msg"));
//        assert mkdir.get("msg") == "创建成功";
//    }
//
//    /**
//     * 测试上传
//     */
//    @Test
//    @Order(2)
//    public void testUpload() throws FileNotFoundException {
//        Map<String, Object> upload = fileService.upload(new UploadedFile("text/html",
//                        new FileInputStream("D:\\System\\Downloads\\quick_start.sh"),
//                        "quick_start.sh"),
//                "/");
//        log.debug("Upload : {}", upload.get("msg"));
//        assert upload.get("msg") == "上传成功";
//    }
//
//    /**
//     * 测试Share
//     */
//    @Test
//    @Order(3)
//    public void testShare() {
//        Map<String, Object> share = fileService.share("quick_start.sh", 30);
//        log.debug("Share : {}", share.get("msg"));
//        assert share.get("msg") == "分享成功";
//    }
//
//    /**
//     * 测试del
//     */
//    @Test
//    @Order(4)
//    public void testDel() {
//        Map<String, Object> del = fileService.del("/quick_start.sh");
//        log.debug("Del : {}", del.get("msg"));
//        assert del.get("msg") == "文件删除成功";
//    }
//
//    /**
//     * 测试List
//     */
//    @Test
//    @Order(5)
//    public void testList() {
//        Map<String, Object> list = fileService.list("/", "", "");
//        log.debug("List Data : {}", list.get("data"));
//        assert list.get("msg") == "查询成功";
//    }
}