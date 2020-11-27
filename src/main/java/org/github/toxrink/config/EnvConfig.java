package org.github.toxrink.config;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;

@Data
@ConfigProperties(prefix = "env")
public class EnvConfig {
    /**
     * 采集器配置文件
     */
    private String collect;

    /**
     * flume组件所需配置
     */
    private String file;

    /**
     * 文件管理历史文件目录
     */
    private String historyFile;

    /**
     * flume的bin目录地址
     */
    private String flumeBinHome;

    /**
     * 程序根目录
     */
    private String baseHome;

    /**
     * 配置文件目录,ambari模式替代baseHome
     */
    private String confHome;

    /**
     * 模板目录
     */
    private String template;

    /**
     * windows启动命令格式
     */
    private String startCmdWin;

    /**
     * linux启动命令格式
     */
    private String startCmdLinux;

    /**
     * windows中的启动cmd
     */
    private String startWin;

    /**
     * linux中的启动shell
     */
    private String startLinux;

    /**
     * 最大运行中agent可用内存
     */
    private long maxAllowMem;

    /**
     * 是否启用权限验证
     */
    private boolean auth;

    /**
     * 是否使用本地验证页面
     */
    private boolean authLocal;

    /**
     * 登录验证字符串
     */
    private String authToken;

    /**
     * 权限验证链接
     */
    private String authUrl;

    /**
     * rmi端口
     */
    private int rmiPort;

    /**
     * 文件改动监听循环时间,单位秒
     */
    private int fileScanInterval;

    /**
     * 配置文件改动自动重启采集器
     */
    private boolean autoRestart;

    /**
     * 是否使用ambari集成
     */
    private boolean ambari;

    /**
     * 是否使用ambari集成
     */
    private boolean jmxMetric;

}
