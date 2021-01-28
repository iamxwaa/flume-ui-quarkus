package org.github.iamxwaa.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class FlumeInfo implements Serializable {

    private static final long serialVersionUID = 2318166982669834406L;

    private String id;

    private String name;

    /**
     * 0关闭 1运行 2重启
     */
    private int state;

    private int pid;

    private String cmd;

    private int writeCount;

    private int readCount;

    private boolean metric;

    private String metricPort;

    private boolean jmxMetric;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = toLinux(cmd);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FlumeInfo [id=" + id + ", name=" + name + ", state=" + state + ", pid=" + pid + ", cmd=" + cmd + "]";
    }

    private String toLinux(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        return path.replace("\\", "/");
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public boolean isMetric() {
        return metric;
    }

    public void setMetric(boolean metric) {
        this.metric = metric;
    }

    public String getMetricPort() {
        return metricPort;
    }

    public void setMetricPort(String metricPort) {
        this.metricPort = metricPort;
    }

    public boolean isJmxMetric() {
        return jmxMetric;
    }

    public void setJmxMetric(boolean jmxMetric) {
        this.jmxMetric = jmxMetric;
    }

}
