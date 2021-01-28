package org.github.iamxwaa;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.github.iamxwaa.config.EnvConfig;
import org.github.iamxwaa.utils.CollectUtils;
import org.github.iamxwaa.utils.EnvUtils;
import org.github.iamxwaa.watcher.CollectorWatcher;
import org.github.iamxwaa.watcher.FileWatcher;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.experimental.PackagePrivate;
import lombok.extern.log4j.Log4j2;

/**
 * 服务启动入口
 * 
 * @author xw
 * 
 *         2020年11月26日
 */

@QuarkusMain
@Log4j2
public class FlumeUi {
    private static EnvConfig staticEnvConfig;

    /**
     * 入口方法
     * 
     * @param args
     *                 启动参数
     */
    public static void main(String[] args) {
        Quarkus.run(FlumeUiApp.class, args);
    }

    public static class FlumeUiApp implements QuarkusApplication {

        @Inject
        @PackagePrivate
        EnvConfig envConfig;

        @Override
        public int run(String... args) throws Exception {
            staticEnvConfig = envConfig;
            // -----------------------初始化-----------------------
            // 初始化文件夹
            EnvUtils.createBaseDir();
            // 配置文件改动监控
            FileWatcher.watch();
            // 采集器状态监控
            CollectorWatcher.watch();
            // 自动启动
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                CollectUtils.getCollectInfoList().forEach(ci -> {
                    if ("on".equals(ci.getAutoStart())) {
                        log.info("Auto start collector " + ci.getId());
                        try {
                            CollectUtils.start(ci.getId());
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                });
            }, 5, TimeUnit.SECONDS);
            Quarkus.waitForExit();
            return 0;
        }
    }

    public static EnvConfig getEnvConfig() {
        return staticEnvConfig;
    }
}
