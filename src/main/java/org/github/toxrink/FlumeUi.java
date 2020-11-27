package org.github.toxrink;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.config.EnvConfig;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.EnvUtils;
import org.github.toxrink.watcher.CollectorWatcher;
import org.github.toxrink.watcher.FileWatcher;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * 服务启动入口
 * 
 * @author xw
 * 
 *         2020年11月26日
 */

@QuarkusMain
public class FlumeUi {
    private static final Log LOG = LogFactory.getLog(FlumeUi.class);

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
        private EnvConfig envConfig;

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
                        LOG.info("Auto start collector " + ci.getId());
                        try {
                            CollectUtils.start(ci.getId());
                        } catch (Exception e) {
                            LOG.error(e);
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