package org.github.toxrink.metric;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.watcher.CollectorWatcher;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flume.instrumentation.ChannelCounterMBean;

import x.os.CmdWrapper;

/**
 * JMXMetric
 */
@Log4j2
public class JMXMetricUtils {
    private static final Map<String, JMXService> jmxServiceMap = Collections.synchronizedMap(new HashMap<>());

    // public static void main(String[] args) throws Exception {
    // JMXService service = new JMXService("");
    // service.connect();
    // System.out.println(getMetricJSON(service));
    // CmdWrapper.sleep(100000);
    // service.close();
    // }

    /**
     * 关闭监控
     * 
     * @param cid
     *                采集器id
     */
    public static void closeJMXService(String cid) {
        JMXService service = jmxServiceMap.remove(cid);
        if (null != service && !service.isClosed) {
            service.close();
        }
    }

    /**
     * 获取监控数据
     * 
     * @param cid
     *                采集器id
     * @return
     */
    public static String getMetricJSON(String cid) {
        if (CollectUtils.isStopping(cid)) {
            return "{}";
        }
        JMXService service = jmxServiceMap.get(cid);
        if (null == service) {
            try {
                CmdWrapper.sleep(10 * 1000);
                synchronized (jmxServiceMap) {
                    service = jmxServiceMap.get(cid);
                    if (null == service) {
                        if (CollectUtils.isStopping(cid)) {
                            return "{}";
                        }
                        if (null == CollectorWatcher.getStatusMap().get(cid)) {
                            return "{}";
                        }
                        service = new JMXService(cid);
                        service.connect();
                        jmxServiceMap.put(cid, service);
                    }
                }
            } catch (IllegalArgumentException e) {
                return "{}";
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return getMetricJSON(service);
    }

    /**
     * 获取监控数据
     * 
     * @param service
     *                    JMXService
     * @return
     */
    public static String getMetricJSON(JMXService service) {
        if (null == service) {
            return "{}";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        try {
            Map<String, ChannelCounterMBean> channelMap = service.getChannelCounterMBeans();
            if (!channelMap.isEmpty() && !service.isClosed) {
                channelMap.forEach((k, v) -> {
                    stringBuilder.append("\"CHANNEL." + k + "\"");
                    stringBuilder.append(":{");
                    stringBuilder.append("\"ChannelCapacity\": \"" + v.getChannelCapacity() + "\",");
                    stringBuilder.append("\"ChannelSize\": \"" + v.getChannelSize() + "\",");
                    stringBuilder.append("\"EventPutSuccessCount\": \"" + v.getEventPutSuccessCount() + "\",");
                    stringBuilder.append("\"EventTakeSuccessCount\": \"" + v.getEventTakeSuccessCount() + "\"");
                    stringBuilder.append("},");
                });
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
        } catch (Exception e) {
            closeJMXService(service.cid);
            log.error("", e);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static class JMXService {
        JMXServiceURL url;
        JMXConnector jmxConnector;
        MBeanServerConnection mbeanServerConnection;
        Map<String, ChannelCounterMBean> channelCounterMBeanMap;
        boolean isClosed = false;
        Integer port;
        String cid;

        JMXService(String cid) throws MalformedURLException, IllegalArgumentException {
            this.cid = cid;
            Integer oport = CollectUtils.getRunningPort(cid);
            if (null == oport) {
                throw new IllegalArgumentException("Cant find running flume info by cid: " + cid);
            }
            port = oport;
        }

        void connect() throws IOException {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi");
            log.info(cid + " create jmx service at " + url.toString());
            jmxConnector = JMXConnectorFactory.newJMXConnector(url, null);
            jmxConnector.connect();
            mbeanServerConnection = jmxConnector.getMBeanServerConnection();
            log.info(cid + " jmx service connected");
        }

        Map<String, ChannelCounterMBean> getChannelCounterMBeans() throws MalformedObjectNameException, IOException {
            if (isClosed || null == mbeanServerConnection) {
                return Collections.emptyMap();
            }
            if (null != channelCounterMBeanMap && !channelCounterMBeanMap.isEmpty()) {
                return channelCounterMBeanMap;
            }
            Set<ObjectInstance> objectInstanceSet = mbeanServerConnection
                    .queryMBeans(new ObjectName("org.apache.flume.channel:type=*"), null);
            channelCounterMBeanMap = new HashMap<>(objectInstanceSet.size());
            objectInstanceSet.forEach(obj -> {
                channelCounterMBeanMap.put(obj.getObjectName().getKeyProperty("type"),
                        MBeanServerInvocationHandler.newProxyInstance(mbeanServerConnection, obj.getObjectName(),
                                ChannelCounterMBean.class, false));
            });
            return channelCounterMBeanMap;
        }

        void close() {
            try {
                jmxConnector.close();
                log.info(cid + " jmx service closed");
            } catch (IOException e) {
                log.error(e);
            }
            isClosed = true;
        }
    }
}