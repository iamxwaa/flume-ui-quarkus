package org.github.toxrink.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.config.EnvConfig;
import org.github.toxrink.metric.JMXMetricUtils;
import org.github.toxrink.metric.MetricUtils;
import org.github.toxrink.model.CollectInfo;
import org.github.toxrink.model.FlumeInfo;
import org.github.toxrink.watcher.CollectorWatcher;

import x.os.CmdWrapper;
import x.os.FileInfo;

/**
 * 采集器相关
 *
 * @author xw
 *
 *         2018年8月3日
 */
public final class CollectUtils {
    private static final Log LOG = LogFactory.getLog(CollectUtils.class);

    /**
     * 重启中的cid
     */
    private static final Set<String> restartSet = new HashSet<String>(8);

    /**
     * 保存采集器数据监控端口
     */
    private static final Map<String, Integer> portMap = new HashMap<>();

    /**
     * 保存运行中的采集器数据监控端口
     */
    private static final Map<String, Integer> runningPortMap = new ConcurrentHashMap<>(8);

    /**
     * 保存关闭中的采集器数据监控端口
     */
    private static final Map<String, Integer> stoppingMap = new ConcurrentHashMap<>(8);

    /**
     * 采集器配置文件获取
     *
     * @param cid
     *                采集id
     * @return 返回采集器配置
     */
    public static List<FileInfo> getCollectFilePath(String cid) {
        return getCollectFilePath(cid, true);
    }

    /**
     * 采集器配置文件获取
     *
     * @param cid
     *                      采集器id
     * @param getConfig
     *                      是否获取flume配置
     * @return
     */
    public static List<FileInfo> getCollectFilePath(String cid, boolean getConfig) {
        List<FileInfo> pathList = new ArrayList<>();
        Optional<CollectInfo> oci = getCollectInfoById(cid);
        if (oci.isPresent()) {
            CollectInfo ci = oci.get();
            try {
                if (getConfig) {
                    File file = File.createTempFile(cid + "_", ".conf");
                    CommonUtils.writeFile(file, ci.getSetting());
                    pathList.add(new FileInfo(file));
                }
                IOUtils.readLines(new ByteArrayInputStream(ci.getSetting().getBytes())).forEach(line -> {
                    int idx = line.indexOf("=");
                    if (-1 != idx && !line.startsWith("#")) {
                        int i1 = line.indexOf(".") + 1;
                        String p2 = line.substring(i1);
                        int i2 = p2.indexOf(".") + 1;
                        String p3 = p2.substring(i2);
                        int i3 = p3.indexOf(".");
                        if (-1 == i3) {
                            return;
                        }
                        String p4 = p3.substring(0, i3);

                        FileInfo fi2 = new FileInfo();
                        String path = line.substring(idx + 1, line.length()).trim();
                        switch (path.charAt(0)) {
                            case '/':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                            case 'g':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                            case 'G':
                                File tmpf = new File(path);
                                if (tmpf.exists() && tmpf.isFile()) {
                                    fi2.setPath(path);
                                    fi2.setName(p4 + "/" + tmpf.getName());
                                    pathList.add(fi2);
                                }
                                break;
                            default:
                        }
                    }

                });
            } catch (IOException e) {
                LOG.error("", e);
            }
        }

        return pathList;
    }

    /**
     * 获取采集器信息
     *
     * @return 返回采集器信息
     */
    public static List<CollectInfo> getCollectInfoList() {
        String path = EnvUtils.getCollectPath();
        return CommonUtils.sort(Arrays.asList(new File(path).listFiles()).stream().map(f -> {
            if (f.getName().endsWith(".json")) {
                CollectInfo ci = CommonUtils.readFileToObject(f, CollectInfo.class);
                return ci;
            } else {
                LOG.warn("ignore collect : " + f.getAbsolutePath());
            }
            return null;
        }).filter(f -> f != null).collect(Collectors.toList()));
    }

    /**
     * 保存采集器信息
     *
     * @param ci
     *               采集器实体类
     * @throws IOException
     *                         写错误
     */
    public static void save(CollectInfo ci) throws IOException {
        fillCollectInfo(ci, true);
        createFlumeConf(ci, true);
        LOG.info("save flume collect to " + ci.getJsonFilePath());
        CommonUtils.writeFile(ci.getJsonFilePath(), ci);
    }

    private static void createFlumeConf(CollectInfo ci, boolean force) throws IOException {
        File file = new File(ci.getConfFilePath());
        if (!file.exists() || force) {
            LOG.info("save flume config to " + ci.getConfFilePath());
            CommonUtils.writeFile(file, ci.getSetting());
        }
    }

    /**
     * 修改采集器信息
     *
     * @param ci
     *               采集器实体类
     * @throws IOException
     *                         写错误
     */
    public static void update(CollectInfo ci) throws IOException {
        fillCollectInfo(ci, false);
        LOG.info("update flume collect config : " + ci.getConfFilePath());
        if (!CommonUtils.readFileToString(ci.getConfFilePath()).equals(ci.getSetting())) {
            LOG.info("backup flume collect config to " + ci.getConfFilePath() + ".bak");
            // 每次修改备份上一次的文件
            CommonUtils.backupFile(ci.getConfFilePath());
        }
        CommonUtils.writeFile(ci.getConfFilePath(), ci.getSetting());
        LOG.info("update flume collect : " + ci.getJsonFilePath());
        CommonUtils.writeFile(ci.getJsonFilePath(), ci);
    }

    /**
     * 补全CollectInfo 必要字段
     *
     * @param ci
     *                   采集器实体类
     * @param autoId
     *                   是否自动生成cid
     */
    public static void fillCollectInfo(CollectInfo ci, boolean autoId) {
        if (StringUtils.isEmpty(ci.getId()) && !autoId) {
            return;
        }
        if (autoId) {
            ci.setId(getCollectId(ci));
        }
        if (StringUtils.isEmpty(ci.getConfFilePath())) {
            ci.setConfFilePath(getConfPath(ci.getId()));
        }
        if (StringUtils.isEmpty(ci.getJsonFilePath())) {
            ci.setJsonFilePath(getJsonFilePath(ci.getId()));
        }
    }

    /**
     * 获取采集器信息文件名
     *
     * @param cid
     *                采集器id
     * @return 返回配置文件路径
     */
    public static String getJsonFilePath(String cid) {
        String path = EnvUtils.getCollectPath();
        return path + "/" + cid + ".json";
    }

    /**
     * 获取采集器id
     *
     * @param ci
     *               采集器实体类
     * @return 返回采集器id
     */
    public static String getCollectId(CollectInfo ci) {
        return CommonUtils.string2MD5(ci.getName() + ci.getDesc() + System.currentTimeMillis());
    }

    /**
     * 获取flume conf文件路径
     *
     * @param cid
     *                采集器id
     * @return 返回配置路径
     */
    public static String getConfPath(String cid) {
        String confPath = EnvUtils.getFlumeConfHomePath();
        return confPath + "/" + cid + ".conf";
    }

    /**
     * 获取配置对象
     *
     * @param cid
     *                采集器id
     * @return 返回采集器对象
     */
    public static Optional<CollectInfo> getCollectInfoById(String cid) {
        if (StringUtils.isEmpty(cid)) {
            return Optional.empty();
        }
        String jsonFilePath = getJsonFilePath(cid);
        CollectInfo ci = CommonUtils.readFileToObject(jsonFilePath, CollectInfo.class);
        ci.setConfFilePath(getConfPath(ci.getId()));
        ci.setJsonFilePath(jsonFilePath);
        return Optional.of(ci);
    }

    /**
     * 获取运行命令
     *
     * @param cid
     *                采集器id
     * @return 返回采集器实体
     * @throws JsonSyntaxException
     *                                 json格式错误
     * @throws IOException
     *                                 读写异常
     */
    public static Optional<String> getStartCmd(String cid) throws JsonSyntaxException, IOException {
        Optional<CollectInfo> ci = getCollectInfoById(cid);
        if (!ci.isPresent()) {
            return Optional.empty();
        }
        Pattern ptn = Pattern.compile("(.*).sources");
        Matcher mat = ptn.matcher(ci.get().getSetting());
        if (mat.find()) {
            boolean isWin = ServerUtils.isWindows();
            EnvConfig path = EnvUtils.getEnvConfig();
            String start = EnvUtils.getFlumeBinHomePath() + "/" + (isWin ? path.getStartWin() : path.getStartLinux());
            String cmd = ServerUtils.isWindows() ? path.getStartCmdWin() : path.getStartCmdLinux();
            cmd = cmd.replace("^START^", start);
            cmd = cmd.replace("^NAME^", mat.group(1));
            cmd = cmd.replace("^CONF^", EnvUtils.getFlumeConfHomePath());
            createFlumeConf(ci.get(), false);
            cmd = cmd.replace("^CONFIG_FILE^", ci.get().getConfFilePath());
            StringBuilder sb = new StringBuilder();
            int metricPort = getMetricPort(ci.get().getId());
            if (isWin) {
                sb.append("flumeCid=" + cid);
                sb.append(" -Xmx" + (StringUtils.isNumeric(ci.get().getMemSize()) ? ci.get().getMemSize() : "2048"));
                sb.append("m");
                sb.append(" -Xms" + (StringUtils.isNumeric(ci.get().getMemSize()) ? ci.get().getMemSize() : "2048"));
                sb.append("m");
                if (path.isJmxMetric()) {
                    sb.append(";com.sun.management.jmxremote");
                    sb.append(";com.sun.management.jmxremote.authenticate=false");
                    sb.append(";com.sun.management.jmxremote.ssl=false");
                    sb.append(";com.sun.management.jmxremote.port=" + metricPort);
                } else {
                    sb.append(";flume.monitoring.type=http");
                    sb.append(";flume.monitoring.port=" + metricPort);
                }
            } else {
                sb.append("-DflumeCid=").append(cid);
                sb.append(" -Xmx" + (StringUtils.isNumeric(ci.get().getMemSize()) ? ci.get().getMemSize() : "2048"));
                sb.append("m");
                sb.append(" -Xms" + (StringUtils.isNumeric(ci.get().getMemSize()) ? ci.get().getMemSize() : "2048"));
                sb.append("m");
                if (path.isJmxMetric()) {
                    sb.append(" -Dcom.sun.management.jmxremote");
                    sb.append(" -Dcom.sun.management.jmxremote.authenticate=false");
                    sb.append(" -Dcom.sun.management.jmxremote.ssl=false");
                    sb.append(" -Dcom.sun.management.jmxremote.port=" + metricPort);
                } else {
                    sb.append(" -Dflume.monitoring.type=http -Dflume.monitoring.port=" + metricPort);
                }
            }
            cmd = cmd.replace("^OTHERS^", sb.toString());
            return Optional.of(cmd);
        }
        LOG.error("no agent name : " + ci.get().getId() + " : " + ci.get().getSetting());
        return Optional.empty();
    }

    /**
     * 启动采集器
     *
     * @param cid
     *                采集器id
     * @return 返回Optional.empty()表示成功
     */
    public static Optional<String> start(String cid) {
        try {
            if (!ServerUtils.getRunningFlumeInfoById(cid).isPresent()) {
                Optional<String> run = getStartCmd(cid);
                if (!run.isPresent()) {
                    LOG.error("cant find agent name !!!");
                    return Optional.of("未找到有效配置的采集器");
                }
                Optional<String> allow = checkMem(cid);
                if (allow.isPresent()) {
                    return allow;
                }
                CmdWrapper.run(run.get()).waitFor(cid, true, 20);
                // 页面倒计时60s
                AtomicInteger cd = new AtomicInteger(60);
                Thread thread = new Thread(() -> {
                    while (cd.get() > 0) {
                        CmdWrapper.sleep(1000);
                        cd.decrementAndGet();
                    }
                });
                thread.start();
                while (cd.get() > 0 && !ServerUtils.getRunningFlumeInfoById(cid).isPresent()) {
                    CmdWrapper.sleep(1000);
                }
                CmdWrapper.sleep(100);
                if (cd.get() < 1) {
                    return Optional.of("采集器启动超时");
                }
                cd.set(0);
            } else {
                LOG.info(cid + " already started !!!");
                return Optional.of("采集器已经启动");
            }
        } catch (Exception e) {
            LOG.error("", e);
            return Optional.of("采集器启动异常");
        }
        LOG.info(ServerUtils.getRunningFlumeInfoById(cid));
        CollectorWatcher.addAutoRestart(cid);
        return Optional.empty();
    }

    /**
     * 关闭采集器
     *
     * @param cid
     *                采集器id
     * @return 返回停止信息
     */
    public static Optional<String> stop(String cid) {
        Optional<FlumeInfo> fi = ServerUtils.getRunningFlumeInfoById(cid);
        try {
            if (fi.isPresent()) {
                CollectorWatcher.removeAutoRestart(cid);
                stoppingMap.put(cid, 1);
                runningPortMap.remove(cid);
                JMXMetricUtils.closeJMXService(cid);
                MetricUtils.removeMetric(cid);
                String cmd = ServerUtils.isWindows() ? "taskkill /F /PID " + fi.get().getPid()
                        : "kill -15 " + fi.get().getPid();
                LOG.info("close collector " + fi.get().getId());
                CmdWrapper.run(cmd);
                // 页面倒计时60s
                AtomicInteger cd = new AtomicInteger(60);
                new Thread(() -> {
                    while (cd.get() > 0) {
                        CmdWrapper.sleep(1000);
                        cd.decrementAndGet();
                    }
                }).start();
                while (cd.get() > 0 && ServerUtils.getRunningFlumeInfoById(cid).isPresent()) {
                    CmdWrapper.sleep(3000);
                }
                CmdWrapper.sleep(100);
                if (cd.get() < 1 && !ServerUtils.isWindows()) {
                    CmdWrapper.run("kill -9 " + fi.get().getPid());
                    return Optional.of("强制关闭");
                }
                cd.set(0);
            } else {
                return Optional.of("采集器未运行");
            }
        } catch (Exception e) {
            LOG.error("", e);
            if (fi.isPresent()) {
                JMXMetricUtils.closeJMXService(cid);
                MetricUtils.removeMetric(cid);
                CmdWrapper.run("kill -9 " + cid);
            }
            return Optional.of("采集器关闭异常");
        } finally {
            stoppingMap.remove(cid);
        }
        return Optional.empty();
    }

    /**
     * 根据id获取最新日志文件,没有返回null
     *
     * @param cid
     *                采集器id
     * @return 日志文件
     */
    public static File getLogFileById(String cid) {
        File file = new File(EnvUtils.getFlumeLogHomePath(cid));
        if (null == file.listFiles()) {
            return null;
        }
        for (File f : file.listFiles()) {
            if (f.getName().contains(cid) && f.getName().endsWith(".log")) {
                return f;
            }
        }
        return null;
    }

    private static Optional<String> checkMem(String cid) {
        long allow = EnvUtils.getEnvConfig().getMaxAllowMem();
        int now = ServerUtils.getRunningFlumeInfoList().values().stream().map(f -> {
            Optional<CollectInfo> ci = getCollectInfoById(f.getId());
            return ci.isPresent() ? Integer.parseInt(ci.get().getMemSize()) : 0;
        }).reduce((a, b) -> a + b).orElse(0);
        Optional<CollectInfo> ci = getCollectInfoById(cid);
        now = Integer.parseInt(ci.get().getMemSize()) + now;
        if (now > allow) {
            return Optional.of("全部运行中的采集器内存超过配置的最大可用内存(" + allow + "MB)");
        }
        return Optional.empty();
    }

    /**
     * 重启采集器
     *
     * @param cid
     *                采集器id
     * @return 重启结果
     */
    public static synchronized boolean restart(String cid) {
        LOG.debug("restart collector, id: " + cid);
        try {
            restartSet.add(cid);
            CollectUtils.stop(cid);
            return !CollectUtils.start(cid).isPresent();
        } finally {
            restartSet.remove(cid);
        }
    }

    /**
     * 是否重启中
     *
     * @param cid
     *                采集id
     * @return true/false
     */
    public static boolean isRestarting(String cid) {
        return restartSet.contains(cid);
    }

    /**
     * 重启中的采集器个数
     *
     * @return 返回个数
     */
    public static int getRestartCount() {
        return restartSet.size();
    }

    /**
     * 获取去重后的端口
     *
     * @param cid
     *                采集器id
     * @return
     */
    private static Integer getMetricPort(String cid) {
        Integer port = portMap.get(cid);
        if (null != port) {
            return port;
        }
        Random rdm = new Random();
        boolean stop = false;
        while (!stop) {
            port = 40000 + rdm.nextInt(500);
            stop = true;
            for (Map.Entry<String, Integer> entry : portMap.entrySet()) {
                if (entry.getValue() == port) {
                    stop = false;
                    break;
                }
            }
        }
        portMap.put(cid, port);
        return port;
    }

    /**
     * 更新运行中的端口
     */
    public static void updateRunningPort(String cid, int port) {
        runningPortMap.put(cid, port);
    }

    /**
     * 获取运行中的端口
     */
    public static Integer getRunningPort(String cid) {
        return runningPortMap.get(cid);
    }

    /**
     * 是否正在关闭
     */
    public static boolean isStopping(String cid) {
        return stoppingMap.get(cid) != null;
    }
}
