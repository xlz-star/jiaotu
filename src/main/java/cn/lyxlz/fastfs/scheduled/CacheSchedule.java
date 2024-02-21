package cn.lyxlz.fastfs.scheduled;

import cn.lyxlz.fastfs.util.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Component
public class CacheSchedule {

    /**
     * 清除过期的分享文件信息, 每3秒钟执行一次
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void clearShareCache() {
        Map<String, Date> dataExpireMap = CacheUtil.dataExpireMap;
        Set<String> keysExpire = dataExpireMap.keySet();
        // 使用并行流加速查询
        keysExpire.parallelStream()
                .filter((t) -> {
                    Date expireDate = CacheUtil.dataExpireMap.get(t);
                    return (expireDate != null && expireDate.compareTo(new Date()) < 0);
                })
                .forEach(CacheUtil::remove);
    }

}
