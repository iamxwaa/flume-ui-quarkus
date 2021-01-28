package org.github.iamxwaa.model;

import org.apache.commons.lang.StringUtils;

public class CollectInfo extends TemplateInfo {

    private static final long serialVersionUID = -2071307452410075633L;

    private String confFilePath;

    private String memSize;

    // on/off
    private String autoStart;

    // on/off
    private String autoRestart;

    // ------------以下为描述性字段----------------

    private String company;

    private String product;

    private String productVersion;

    public String getConfFilePath() {
        return toLinux(confFilePath);
    }

    public void setConfFilePath(String confFilePath) {
        this.confFilePath = toLinux(confFilePath);
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String getMemSize() {
        return memSize;
    }

    public void setMemSize(String memSize) {
        this.memSize = memSize;
    }

    /**
     * 是否支持数据监控
     * 
     * @return true/false
     */
    public boolean isSupportMetric() {
        if (StringUtils.isEmpty(getSetting())) {
            return false;
        }
        return getSetting().contains("MemoryCountChannel");
    }

    public String getAutoStart() {
        return autoStart;
    }

    public void setAutoStart(String autoStart) {
        this.autoStart = autoStart;
    }

    public String getAutoRestart() {
        return autoRestart;
    }

    public void setAutoRestart(String autoRestart) {
        this.autoRestart = autoRestart;
    }
}
