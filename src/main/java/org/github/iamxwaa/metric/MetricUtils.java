package org.github.iamxwaa.metric;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import lombok.extern.log4j.Log4j2;
import x.rmi.RemoteWrapper;
import x.rmi.RemoteWrapper.RemoteOne;

@Log4j2
public final class MetricUtils {
    private static final Map<String, Counter> countMap = new HashMap<>();

    public static final String NAME = "Flume-RMICounter";

    private static final Gson GSON = new Gson();

    private static RemoteOne remoteOne = null;

    /**
     * 获取全部信息
     * 
     * @return
     */
    public static String getAll() {
        return GSON.toJson(countMap);
    }

    /**
     * 根据cid获取计数器
     * 
     * @param cid
     *                采集器id
     * @return
     */
    public static Counter getCounterByCid(String cid) {
        Counter count = countMap.get(cid);
        if (null == count) {
            count = new Counter();
            countMap.put(cid, count);
        }
        return count;
    }

    /**
     * 开始数值监控
     * 
     * @param port
     *                 监控端口
     */
    public static void startMetric(int port) {
        try {
            RMICounterService service = new RMICounterServiceImpl();
            remoteOne = RemoteWrapper.wrap(NAME, null, port, service);
            log.info("启动数据状态监控,端口: " + port);
            remoteOne.getServer().start();
        } catch (RemoteException e) {
            log.error("", e);
        }
    }

    /**
     * 移除监控计数器
     * 
     * @param cid
     *                采集器id
     */
    public static void removeMetric(String cid) {
        countMap.remove(cid);
    }

}
