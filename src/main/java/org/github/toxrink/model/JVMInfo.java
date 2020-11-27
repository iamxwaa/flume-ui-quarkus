package org.github.toxrink.model;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;

import x.utils.TimeUtils;

/**
 * JVMInfo
 */
public class JVMInfo {

    private String name;
    private String startTime;
    private String vmVersion;
    private int cpu;
    private String heap;

    /**
     * JVMInfo实例
     */
    public JVMInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.name = runtimeMXBean.getName();
        this.startTime = TimeUtils.format2(new Date(runtimeMXBean.getStartTime()));
        this.vmVersion = runtimeMXBean.getSpecVendor() + " JDK " + runtimeMXBean.getSpecVersion();

        this.cpu = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        this.heap = toMB(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()) + "/"
                + toMB(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
    }

    private String toMB(long m) {
        return m / 1024 / 1024 + " MB";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getVmVersion() {
        return vmVersion;
    }

    public void setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public String getHeap() {
        return heap;
    }

    public void setHeap(String heap) {
        this.heap = heap;
    }
}