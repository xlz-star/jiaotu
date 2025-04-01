package cn.lyxlz.fastfs;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;

/**
 * 主程序入口
 *
 * @author xlz
 * @date 2023/09/02
 */
@SolonMain
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args);
    }
}