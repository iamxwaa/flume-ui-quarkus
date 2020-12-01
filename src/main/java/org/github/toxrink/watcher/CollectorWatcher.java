package org.github.toxrink.watcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.metric.JMXMetricUtils;
import org.github.toxrink.model.FlumeCheck;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.EnvUtils;
import org.github.toxrink.utils.ServerUtils;

/**
 * 监控flume运行状态
 * 
 * @author xw
 * 
 *         2020年06月28日
 */
public class CollectorWatcher {
    private static final Log LOG = LogFactory.getLog(CollectorWatcher.class);

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 保存运行状态
     */
    private static Map<String, FlumeCheck> statusMap = new HashMap<>();

    /**
     * 保存停止后自动重启的对象
     */
    private static Map<String, FlumeCheck> autoRestartMap = new HashMap<>();

    /**
     * 是否正在更新状态
     */
    private static boolean updating = false;

    /**
     * 上传更新状态的时间
     */
    private static long updateTime = 0;

    /**
     * 允许服务连续挂掉重启的时间间隔
     */
    public static final long ALLOW_RANGE = 30 * 60 * 1000;

    /**
     * 允许间隔内重启重试最大次数
     */
    public static final int RETRY = 2;

    private static void putLinuxStatus(Map<String, FlumeCheck> map) throws IOException {
        Process proc = Runtime.getRuntime().exec(new String[] { "ps", "-ef" });
        Pattern ptn = Pattern.compile("([0-9a-z]{32}).conf");
        Pattern ptn2 = EnvUtils.getEnvConfig().isJmxMetric()
                ? Pattern.compile("com.sun.management.jmxremote.port=(\\d{3,5})")
                : Pattern.compile("flume.monitoring.port=(\\d{3,5})");
        long checkTime = System.currentTimeMillis();
        for (String p : IOUtils.readLines(proc.getInputStream(), EnvUtils.UTF8)) {
            if (!p.contains("DflumeCid")) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(p);
            }
            String[] tmp = p.split("org.apache.flume.node.Application");
            int start = -1;
            int end = -1;
            if (tmp.length > 1) {
                for (int i = 1; i < tmp[0].length(); i++) {
                    char c = tmp[0].charAt(i);
                    if (tmp[0].charAt(i) == ' ') {
                        start = -1;
                        continue;
                    }
                    if (start == -1 && c >= '0' && c <= '9' && tmp[0].charAt(i - 1) == ' ') {
                        start = i;
                        continue;
                    }
                    if (start != -1 && c >= '0' && c <= '9' && tmp[0].charAt(i + 1) == ' ') {
                        end = i + 1;
                        break;
                    }
                }
                String pid = tmp[0].substring(start, end);
                Matcher mat = ptn.matcher(tmp[1]);
                if (mat.find()) {
                    FlumeCheck flumeCheck = new FlumeCheck();
                    flumeCheck.setId(mat.group(1));
                    flumeCheck.setPid(Integer.parseInt(pid));
                    flumeCheck.setState(1);
                    flumeCheck.setCheckTime(checkTime);
                    Matcher mat2 = ptn2.matcher(p);
                    if (mat2.find()) {
                        flumeCheck.setMetricPort(mat2.group(1));
                        flumeCheck.setMetric(true);
                        CollectUtils.updateRunningPort(flumeCheck.getId(),
                                Integer.parseInt(flumeCheck.getMetricPort()));
                        flumeCheck.setCmd(CollectUtils.getStartCmd(flumeCheck.getId()).get());
                    }
                    map.put(flumeCheck.getId(), flumeCheck);
                }
            }
        }
    }

    private static void putWindowsStatusByJPS(Map<String, FlumeCheck> map) throws IOException {
        // 首先获取自定义参数中的java home
        String javaHome = System.getenv("USER_JAVA_HOME");
        if (StringUtils.isEmpty(javaHome)) {
            javaHome = System.getenv("JAVA_HOME");
        }
        String cmd = null == javaHome ? "jps -lm"
                : new StringBuilder().append(javaHome).append(File.separator).append("bin").append(File.separator)
                        .append("jps -lm").toString();
        Process proc = Runtime.getRuntime().exec(cmd);
        Pattern ptn = Pattern.compile("([0-9a-z]{32}).conf");
        Pattern ptn2 = EnvUtils.getEnvConfig().isJmxMetric()
                ? Pattern.compile("com.sun.management.jmxremote.port=(\\d{3,5})")
                : Pattern.compile("flume.monitoring.port=(\\d{3,5})");
        long checkTime = System.currentTimeMillis();
        for (String p : IOUtils.readLines(proc.getInputStream(), EnvUtils.UTF8)) {
            String[] tmp = p.split("org.apache.flume.node.Application");
            if (tmp.length > 1 && StringUtils.isNumeric(tmp[0].trim())) {
                Matcher mat = ptn.matcher(tmp[1]);
                if (mat.find()) {
                    FlumeCheck flumeCheck = new FlumeCheck();
                    flumeCheck.setId(mat.group(1));
                    flumeCheck.setPid(Integer.parseInt(tmp[0].trim()));
                    flumeCheck.setState(1);
                    flumeCheck.setCheckTime(checkTime);
                    Matcher mat2 = ptn2.matcher(p);
                    if (mat2.find()) {
                        flumeCheck.setMetricPort(mat2.group(1));
                        flumeCheck.setMetric(true);
                        CollectUtils.updateRunningPort(flumeCheck.getId(),
                                Integer.parseInt(flumeCheck.getMetricPort()));
                        flumeCheck.setCmd(CollectUtils.getStartCmd(flumeCheck.getId()).get());
                    }
                    map.put(flumeCheck.getId(), flumeCheck);
                }
            }
        }
    }

    private static void putWindowsStatus(Map<String, FlumeCheck> map) throws IOException {
        String command = "wmic process where caption=\"java.exe\" get commandline,processid /value";
        Process proc = Runtime.getRuntime().exec(command);
        Pattern ptn = Pattern.compile("([0-9a-z]{32}).conf");
        Pattern ptn2 = EnvUtils.getEnvConfig().isJmxMetric()
                ? Pattern.compile("com.sun.management.jmxremote.port=(\\d{3,5})")
                : Pattern.compile("flume.monitoring.port=(\\d{3,5})");
        FlumeCheck flumeCheck = null;
        boolean getPid = false;
        long checkTime = System.currentTimeMillis();
        for (String p : IOUtils.readLines(proc.getInputStream(), EnvUtils.UTF8)) {
            if (StringUtils.isEmpty(p)) {
                continue;
            }
            if (p.startsWith("CommandLine") && p.contains("DflumeCid")) {
                Matcher mat = ptn.matcher(p);
                Matcher mat2 = ptn2.matcher(p);
                if (mat.find()) {
                    flumeCheck = new FlumeCheck();
                    flumeCheck.setId(mat.group(1));
                    getPid = true;
                }
                if (mat2.find()) {
                    flumeCheck.setMetricPort(mat2.group(1));
                    flumeCheck.setMetric(true);
                    flumeCheck.setCmd(CollectUtils.getStartCmd(flumeCheck.getId()).get());
                }
                continue;
            }
            if (getPid && p.startsWith("ProcessId")) {
                flumeCheck.setPid(Integer.parseInt(p.substring(10)));
                flumeCheck.setState(1);
                flumeCheck.setCheckTime(checkTime);
                CollectUtils.updateRunningPort(flumeCheck.getId(), Integer.parseInt(flumeCheck.getMetricPort()));
                map.put(flumeCheck.getId(), flumeCheck);
                getPid = false;
            }
        }
    }

    /**
     * 启动监控
     */
    public static void watch() {
        LOG.info("启动采集器状态监控");
        executor.scheduleWithFixedDelay(() -> {
            if (updating) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("正在更新状态,上次更新时间" + updateTime);
                }
                return;
            }
            try {
                statusMap = getRunningCollector();
                statusMap.forEach((k, v) -> {
                    switch (v.getState()) {
                        case 0:
                            JMXMetricUtils.closeJMXService(k);
                            break;
                        case 1:
                            // 加载已经启动但未添加自动重启监控的采集器
                            if (!autoRestartMap.containsKey(k)) {
                                addAutoRestart(k);
                            }
                            JMXMetricUtils.getMetricJSON(k);
                            break;
                        default:// 不做处理
                    }
                });
                autoRestartMap.forEach((k, v) -> {
                    if (v.isIgnore()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("超过连续重试自动重启次数" + v.getRetry() + ",请手动启动: " + k);
                        }
                        return;
                    }
                    FlumeCheck flumeCheck = statusMap.get(k);
                    if (CollectUtils.isStopping(k)) {
                        LOG.info("手动关闭中,忽略自动重启:" + k);
                        return;
                    }
                    // 服务挂了,重启
                    if (null == flumeCheck || 0 == flumeCheck.getState()) {
                        new Thread(() -> {
                            if (v.isRestarting()) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("重启中:" + k);
                                }
                                return;
                            }
                            LOG.warn("采集器" + k + "已停止运行,尝试自动重启.");
                            try {
                                v.setRestarting(true);
                                JMXMetricUtils.closeJMXService(k);
                                if (v.getRetry() > RETRY) {
                                    LOG.error("超过连续重试自动重启次数" + v.getRetry() + ",请手动启动: " + k);
                                    v.setIgnore(true);
                                    return;
                                }
                                long now = System.currentTimeMillis();
                                if (now - v.getLastRestartTime() > ALLOW_RANGE) {
                                    v.setRestartTimes(v.getRestartTimes() + 1);
                                    v.setRetry(1);
                                } else {
                                    v.setRestartTimes(v.getRestartTimes() + 1);
                                    v.setRetry(v.getRetry() + 1);
                                }
                                v.setLastRestartTime(now);
                                CollectUtils.restart(k);
                            } finally {
                                v.setRestarting(false);
                            }
                        }).start();
                    } else {
                        v.setPid(flumeCheck.getPid());
                    }
                });
            } catch (Exception e) {
                LOG.error("", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 更新采集器运行状态
     * 
     * @return
     */
    public static Map<String, FlumeCheck> getRunningCollector() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("更新采集器运行状态");
        }
        Map<String, FlumeCheck> map = new HashMap<>();
        try {
            updating = true;
            // linux系统采集ps命令解析
            if (ServerUtils.isWindows()) {
                try {
                    putWindowsStatus(map);
                } catch (IOException e) {
                    try {
                        putWindowsStatusByJPS(map);
                    } catch (IOException e1) {
                        LOG.error("", e1);
                    }
                }
            } else {
                try {
                    putLinuxStatus(map);
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
            updateTime = System.currentTimeMillis();
        } finally {
            updating = false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("更新采集器运行状态完毕");
        }
        return map;
    }

    /**
     * 获取采集器运行状态
     * 
     * @return
     */
    public static Map<String, FlumeCheck> getStatusMap() {
        return statusMap;
    }

    /**
     * 获取需要自动重启的采集器
     * 
     * @return
     */
    public static Map<String, FlumeCheck> getAutoRestartMap() {
        return autoRestartMap;
    }

    /**
     * 是否正在更新状态
     * 
     * @return
     */
    public static boolean isUpdating() {
        return updating;
    }

    /**
     * 状态更新时间
     * 
     * @return
     */
    public static long getUpdateTime() {
        return updateTime;
    }

    /**
     * 添加自动重启
     * 
     * @param cid
     *                id
     */
    public static void addAutoRestart(String cid) {
        FlumeCheck flumeCheck = autoRestartMap.get(cid);
        if (null != flumeCheck && !flumeCheck.isIgnore()) {
            return;
        }
        if (CollectUtils.isStopping(cid)) {
            LOG.info("手动关闭中,取消添加采集器进程守护:" + cid);
            return;
        }
        LOG.info("添加采集器进程守护: " + cid);
        FlumeCheck flumeCheck2 = new FlumeCheck();
        flumeCheck2.setId(cid);
        flumeCheck2.setIgnore(false);
        if (null != flumeCheck) {
            flumeCheck2.setRestartTimes(flumeCheck.getRestartTimes());
        }
        autoRestartMap.put(cid, flumeCheck2);
    }

    /**
     * 移除自动重启
     * 
     * @param cid
     *                id
     */
    public static void removeAutoRestart(String cid) {
        LOG.info("移除采集器进程守护: " + cid);
        autoRestartMap.remove(cid);
    }
}