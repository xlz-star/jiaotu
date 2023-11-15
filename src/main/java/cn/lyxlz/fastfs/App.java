package cn.lyxlz.fastfs;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.boot.http.HttpServerConfigure;

import java.util.concurrent.Executors;

/**
 * 主程序入口
 *
 * @author xlz
 * @date 2023/09/02
 */
@SolonMain
public class App {

    public static void main(String[] args) {
        Solon.start(App.class, args, app-> app.onEvent(HttpServerConfigure.class, e -> {
            // 启用虚拟线程
            e.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        }));
    }

}