package org.github.toxrink.utils;

import java.io.File;
import java.nio.charset.Charset;

import com.google.common.collect.ImmutableList;

import org.github.toxrink.FlumeUi;
import org.github.toxrink.config.EnvConfig;

import lombok.extern.log4j.Log4j2;

/**
 * 路径处理
 * 
 * @author xw
 *
 *         2018年8月2日
 */
@Log4j2
public final class EnvUtils {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * 创建基础目录
     */
    public static void createBaseDir() {
        ImmutableList.of(getBaseHomePath(), getAmbariPath(), getFilePath(), getCollectPath(), getTemplatePath())
                .forEach(path -> {
                    File file = new File(path);
                    if (!file.exists()) {
                        log.info("create dir " + file.getAbsolutePath());
                        file.mkdirs();
                    }
                });
        UploadUtils.createSubDir();
    }

    /**
     * 获取配置
     * 
     * @return 配置对象
     */
    public static EnvConfig getEnvConfig() {
        return FlumeUi.getEnvConfig();
    }

    /**
     * 获取该工程家目录
     * 
     * @return 家目录
     */
    public static String getBaseHomePath() {
        EnvConfig path = getEnvConfig();
        return path.getBaseHome();
    }

    /**
     * 获取ambari集成配置文件目录
     * 
     * @return ambari配置目录
     */
    public static String getAmbariPath() {
        EnvConfig path = getEnvConfig();
        return path.getConfHome();
    }

    /**
     * 获取模板目录
     * 
     * @return 模板目录
     */
    public static String getTemplatePath() {
        EnvConfig path = getEnvConfig();
        if (path.isAmbari()) {
            return getAmbariPath() + path.getTemplate();
        }
        return getBaseHomePath() + path.getTemplate();
    }

    /**
     * 获取上传文件目录
     * 
     * @return 文件上传目录
     */
    public static String getFilePath() {
        EnvConfig path = getEnvConfig();
        if (path.isAmbari()) {
            return getAmbariPath() + path.getFile();
        }
        return getBaseHomePath() + path.getFile();
    }

    /**
     * 获取历史文件目录
     *
     * @return 历史目录
     */
    public static String getHistoryFilePath() {
        EnvConfig path = getEnvConfig();
        if (path.isAmbari()) {
            return getAmbariPath() + path.getHistoryFile();
        }
        return getBaseHomePath() + path.getHistoryFile();
    }

    /**
     * 获取采集器配置目录
     * 
     * @return 采集器目录
     */
    public static String getCollectPath() {
        EnvConfig path = getEnvConfig();
        if (path.isAmbari()) {
            return getAmbariPath() + path.getCollect();
        }
        return getBaseHomePath() + path.getCollect();
    }

    /**
     * 获取 flume bin 目录
     * 
     * @return flume/bin路径
     */
    public static String getFlumeBinHomePath() {
        EnvConfig path = getEnvConfig();
        File tmp = new File(path.getFlumeBinHome());
        if (tmp.exists() && tmp.isDirectory()) {
            return path.getFlumeBinHome();
        }
        return getBaseHomePath() + path.getFlumeBinHome();
    }

    /**
     * 获取flume conf目录
     * 
     * @return flume/conf路径
     */
    public static String getFlumeConfHomePath() {
        EnvConfig path = getEnvConfig();
        StringBuilder sb = new StringBuilder(getBaseHomePath() + path.getFlumeBinHome());
        sb.delete(sb.length() - 3, sb.length());
        sb.append("conf");
        return sb.toString();
    }

    /**
     * 获取flume log目录
     * 
     * @return 日志目录
     */
    public static String getFlumeLogHomePath(String cid) {
        String ambariPath = "/var/log/vap-flume/" + cid;
        if (new File(ambariPath).exists()) {
            return ambariPath;
        }
        // windows上可能存放的路径
        String windows = new File(getBaseHomePath()).toPath().getRoot() + "\\var\\log\\vap-flume\\" + cid;
        if (new File(windows).exists()) {
            return windows;
        }
        StringBuilder sb = new StringBuilder(getBaseHomePath());
        sb.append("/logs/");
        sb.append(cid);
        sb.append("/");
        return sb.toString();
    }

    /**
     * 获取flume ui log
     * 
     * @return 页面日志路径
     */
    public static String getFlumeUILog() {
        String ambariPath = "/var/log/vap-flume/flume-ui.log";
        if (new File(ambariPath).exists()) {
            return ambariPath;
        }
        // windows上可能存放的路径
        String windows = new File(getBaseHomePath()).toPath().getRoot() + "\\var\\log\\vap-flume\\flume-ui.log";
        if (new File(windows).exists()) {
            return windows;
        }
        StringBuilder sb = new StringBuilder(getBaseHomePath());
        sb.append("/flume-ui.log");
        return sb.toString();
    }
}
