package org.github.iamxwaa.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.github.iamxwaa.model.CollectInfo;
import org.github.iamxwaa.model.UsingFileInfo;
import org.github.iamxwaa.utils.CollectUtils;
import org.github.iamxwaa.utils.EnvUtils;
import org.github.iamxwaa.utils.ServerUtils;

import lombok.extern.log4j.Log4j2;
import x.utils.TimeUtils;

/**
 * 监听配置文件改动
 * 
 * @author xw
 *
 *         2019年1月18日
 */
@Log4j2
public class FileWatcher {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 保存每个采集器使用的文件
     */
    private static final Map<String, UsingFileInfo> ufiMap = new HashMap<String, UsingFileInfo>();

    /**
     * 监听改动的文件名后缀
     */
    private static final String[] FILE_SUFFIX = new String[] { ".js", ".xml", ".yml", ".yaml", ".conf", ".txt", ".csv",
            ".properties", ".json", ".avsc", ".avdl" };

    /**
     * 启动失败时最大重试次数
     */
    private static final int RETRY = 3;

    /**
     * 开启配置改动监听
     */
    public static void watch() {
        // 根据配置开启是否监控全部采集器还是部分采集器
        if (!EnvUtils.getEnvConfig().isAutoRestart()) {
            watch2();
            return;
        }
        log.info("开启(全部采集器)配置文件改动监听");
        executor.scheduleWithFixedDelay(() -> {
            if (log.isDebugEnabled()) {
                log.debug("refresh using file info");
            }
            Map<String, UsingFileInfo> ufiMapTmp = new HashMap<String, UsingFileInfo>();
            // 获取正在使用的配置
            ServerUtils.getRunningFlumeInfoList().forEach((k, v) -> {
                for (x.os.FileInfo f : CollectUtils.getCollectFilePath(k, false)) {
                    UsingFileInfo ufi = new UsingFileInfo();
                    File file = new File(f.getPath());
                    ufi.setCid(k);
                    ufi.setModifyTime(TimeUtils.format2(new Date(file.lastModified())));
                    ufi.setPath(file.getAbsolutePath());
                    for (String end : FILE_SUFFIX) {
                        if (file.getName().endsWith(end)) {
                            ufiMapTmp.put(getKey(ufi), ufi);
                            break;
                        }
                    }
                }
            });

            // 合并记录
            List<String> unuseList = new ArrayList<String>();
            // 保存该轮已经重启过的采集器,防止重复重启
            Set<String> already = new HashSet<String>();
            ufiMap.forEach((k, v) -> {
                if (ufiMapTmp.containsKey(k)) {
                    UsingFileInfo ufiTmp = ufiMapTmp.get(k);
                    // 修改时间不一致,更新配置并重启采集器
                    if (!v.getModifyTime().equals(ufiTmp.getModifyTime())) {
                        ufiMap.put(k, ufiTmp);
                        // 配置文件改动,重启采集器
                        if (!already.contains(ufiTmp.getCid())) {
                            log.info(ufiTmp.getPath() + " was changed , restart collector.");
                            int i = 0;
                            while (i < RETRY) {
                                if (CollectUtils.restart(ufiTmp.getCid())) {
                                    already.add(ufiTmp.getCid());
                                    break;
                                }
                                i++;
                            }
                            if (i >= RETRY) {
                                log.error(">> " + ufiTmp.getCid() + " << start fail " + i + " times !!!");
                            }
                        }
                    }
                } else {
                    unuseList.add(k);
                }
            });
            // 移除未使用的配置
            unuseList.forEach(a -> {
                ufiMap.remove(a);
            });
            ufiMapTmp.forEach((k, v) -> {
                // 合并新增的在使用的配置
                if (!ufiMap.containsKey(k)) {
                    ufiMap.put(k, v);
                }
            });

        }, 5, EnvUtils.getEnvConfig().getFileScanInterval(), TimeUnit.SECONDS);
    }

    /**
     * 监听启用自动重启配置的采集器
     */
    public static void watch2() {
        log.info("开启配置文件改动监听");
        executor.scheduleWithFixedDelay(() -> {
            if (log.isDebugEnabled()) {
                log.debug("refresh using file info");
            }
            Map<String, UsingFileInfo> ufiMapTmp = new HashMap<String, UsingFileInfo>();
            // 获取正在使用的配置
            ServerUtils.getRunningFlumeInfoList().forEach((k, v) -> {
                Optional<CollectInfo> optCollectInfo = CollectUtils.getCollectInfoById(k);
                // 采集器不存在或不需要自动重启则跳过
                if (!optCollectInfo.isPresent() || !"on".equals(optCollectInfo.get().getAutoRestart())) {
                    return;
                }
                for (x.os.FileInfo f : CollectUtils.getCollectFilePath(k, false)) {
                    UsingFileInfo ufi = new UsingFileInfo();
                    File file = new File(f.getPath());
                    ufi.setCid(k);
                    ufi.setModifyTime(TimeUtils.format2(new Date(file.lastModified())));
                    ufi.setPath(file.getAbsolutePath());
                    for (String end : FILE_SUFFIX) {
                        if (file.getName().endsWith(end)) {
                            ufiMapTmp.put(getKey(ufi), ufi);
                            break;
                        }
                    }
                }
            });

            // 合并记录
            List<String> unuseList = new ArrayList<String>();
            // 保存该轮已经重启过的采集器,防止重复重启
            Set<String> already = new HashSet<String>();
            ufiMap.forEach((k, v) -> {
                if (ufiMapTmp.containsKey(k)) {
                    UsingFileInfo ufiTmp = ufiMapTmp.get(k);
                    // 修改时间不一致,更新配置并重启采集器
                    if (!v.getModifyTime().equals(ufiTmp.getModifyTime())) {
                        ufiMap.put(k, ufiTmp);
                        // 配置文件改动,重启采集器
                        if (!already.contains(ufiTmp.getCid())) {
                            log.info(ufiTmp.getPath() + " was changed , restart collector.");
                            int i = 0;
                            while (i < RETRY) {
                                if (CollectUtils.restart(ufiTmp.getCid())) {
                                    already.add(ufiTmp.getCid());
                                    break;
                                }
                                i++;
                            }
                            if (i >= RETRY) {
                                log.error(">> " + ufiTmp.getCid() + " << start fail " + i + " times !!!");
                            }
                        }
                    }
                } else {
                    unuseList.add(k);
                }
            });
            // 移除未使用的配置
            unuseList.forEach(a -> {
                ufiMap.remove(a);
            });
            ufiMapTmp.forEach((k, v) -> {
                // 合并新增的在使用的配置
                if (!ufiMap.containsKey(k)) {
                    ufiMap.put(k, v);
                }
            });

        }, 5, EnvUtils.getEnvConfig().getFileScanInterval(), TimeUnit.SECONDS);
    }

    /**
     * 获取正在使用的配置
     * 
     * @return
     */
    public static List<UsingFileInfo> getUsingFileList() {
        List<UsingFileInfo> list = new ArrayList<UsingFileInfo>();
        list.addAll(ufiMap.values());
        list.sort((a, b) -> a.getCid().compareTo(b.getCid()));
        return list;
    }

    private static String getKey(UsingFileInfo ufi) {
        return ufi.getCid() + ":" + ufi.getPath();
    }
}
