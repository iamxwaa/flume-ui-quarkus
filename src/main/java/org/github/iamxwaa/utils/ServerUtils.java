package org.github.iamxwaa.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.github.iamxwaa.model.FlumeCheck;
import org.github.iamxwaa.model.FlumeInfo;
import org.github.iamxwaa.watcher.CollectorWatcher;

import lombok.extern.log4j.Log4j2;
import x.os.CmdWrapper;

/**
 * 程序运行状态
 * 
 * @author xw
 *
 *         2018年8月2日
 */
@Log4j2
public final class ServerUtils {
    /**
     * 获取采集器运行情况
     * 
     * @return 运行情况列表
     */
    public static List<FlumeInfo> getFlumeInfoList() {
        Map<String, FlumeCheck> run = getRunningFlumeInfoList();
        List<FlumeInfo> all = CollectUtils.getCollectInfoList().stream().map(f -> {
            FlumeInfo fi = new FlumeInfo();
            fi.setId(f.getId());
            fi.setName(f.getName());
            fi.setPid(-1);
            fi.setState(0);
            fi.setMetric(f.isSupportMetric());
            FlumeInfo tmp = run.get(fi.getId());
            if (null != tmp) {
                fi.setPid(tmp.getPid());
                fi.setState(1);
                fi.setCmd(tmp.getCmd());
                fi.setMetric(tmp.isMetric());
                fi.setMetricPort(tmp.getMetricPort());
            } else {
                Optional<String> cmd;
                try {
                    cmd = CollectUtils.getStartCmd(f.getId());
                    fi.setCmd(cmd.orElse("error"));
                } catch (JsonSyntaxException | IOException e) {
                    log.error("", e);
                }
            }
            if (CollectUtils.isRestarting(fi.getId())) {
                fi.setState(2);
            }
            fi.setCmd(makeBeautyCmd(fi.getCmd()));
            return fi;
        }).collect(Collectors.toList());
        all.sort((a, b) -> {
            return a.getState() > b.getState() ? -1 : (a.getState() == b.getState() ? 0 : 1);
        });
        return all;
    }

    /**
     * 获取运行的采集器
     * 
     * @return Map(采集器id,运行信息)
     * @throws IOException
     *                         控制台错误
     */
    public static Map<String, FlumeCheck> getRunningFlumeInfoList() {
        while (CollectorWatcher.isUpdating()) {
            CmdWrapper.sleep(100);
        }
        return CollectorWatcher.getStatusMap();
    }

    /**
     * 根据配置文件名获取运行情况
     * 
     * @param cid
     *                采集器id
     * @return 运行信息
     */
    public static Optional<FlumeInfo> getRunningFlumeInfoById(String cid) {
        FlumeInfo f = getRunningFlumeInfoList().get(cid);
        Optional<FlumeInfo> ret = f == null ? Optional.empty() : Optional.of(f);
        return ret;
    }

    /**
     * 是否windows平台
     * 
     * @return true/false
     */
    public static boolean isWindows() {
        return CmdWrapper.isWindows();
    }

    private static String makeBeautyCmd(String cmd) {
        if (StringUtils.isEmpty(cmd)) {
            return cmd;
        }
        String[] cmds = cmd.split("\\s");
        StringBuilder sb = new StringBuilder();
        sb.append("<code class=\"code-cmd\">");
        sb.append(cmds[0]);
        sb.append("</code>");
        for (int i = 1; i < cmds.length; i++) {
            sb.append(makeHightlight(cmds[i]));
        }
        return sb.toString();
    }

    private static String makeHightlight(String s) {
        if (s.startsWith("--")) {
            return "<code class=\"code-key\"> " + s + "</code>";
        }
        return "<code class=\"code-value\"> " + s + "</code>";
    }

}
