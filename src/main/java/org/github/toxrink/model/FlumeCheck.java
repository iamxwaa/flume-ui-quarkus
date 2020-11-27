package org.github.toxrink.model;

import java.util.Date;

import x.utils.TimeUtils;

/**
 * 运行详情
 * 
 * @author xw
 * 
 *         2020年06月28日
 */
public class FlumeCheck extends FlumeInfo {

    /**
     *
     */
    private static final long serialVersionUID = 7033697345893276101L;

    /**
     * 检查的运行时间
     */
    private long checkTime;

    /**
     * 重试次数
     */
    private int retry;

    /**
     * 自动重启次数
     */
    private int restartTimes;

    /**
     * 上次重启时间
     */
    private long lastRestartTime;

    /**
     * 是否正在重启
     */
    private boolean restarting;

    /**
     * 忽略自动重启
     */
    private boolean ignore = false;

    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public int getRestartTimes() {
        return restartTimes;
    }

    public void setRestartTimes(int restartTimes) {
        this.restartTimes = restartTimes;
    }

    public String getCheckTimeString() {
        return TimeUtils.format2(new Date(checkTime));
    }

    public long getLastRestartTime() {
        return lastRestartTime;
    }

    public String getLastRestartTimeString() {
        return TimeUtils.format2(new Date(lastRestartTime));
    }

    public void setLastRestartTime(long lastRestartTime) {
        this.lastRestartTime = lastRestartTime;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public boolean isRestarting() {
        return restarting;
    }

    public void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

}