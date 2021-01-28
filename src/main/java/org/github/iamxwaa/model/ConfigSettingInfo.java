package org.github.iamxwaa.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图形配置
 */
public class ConfigSettingInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2859291635829873168L;
    
    private String[] channels;
    private String[] sources;
    private String[] sinks;
    /**
     * source->interceptors
     */
    private Map<String,List<String>> interceptors = new HashMap<>();

    private Map<String, Map<String,String>> channelsField = new HashMap<>();
    private Map<String, Map<String,String>> sourcesField = new HashMap<>();
    private Map<String, Map<String,String>> sinksField = new HashMap<>();
    private Map<String, Map<String,String>> interceptorField = new HashMap<>();

    /**
     * 配置文件的内容
     */
    private String settings;

    public String[] getChannels() {
        return channels;
    }

    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
    }

    public String[] getSinks() {
        return sinks;
    }

    public void setSinks(String[] sinks) {
        this.sinks = sinks;
    }

    public Map<String, Map<String, String>> getChannelsField() {
        return channelsField;
    }

    public void setChannelsField(Map<String, Map<String, String>> channelsField) {
        this.channelsField = channelsField;
    }

    public Map<String, Map<String, String>> getSourcesField() {
        return sourcesField;
    }

    public void setSourcesField(Map<String, Map<String, String>> sourcesField) {
        this.sourcesField = sourcesField;
    }

    public Map<String, Map<String, String>> getSinksField() {
        return sinksField;
    }

    public void setSinksField(Map<String, Map<String, String>> sinksField) {
        this.sinksField = sinksField;
    }

    public Map<String, List<String>> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(Map<String, List<String>> interceptors) {
        this.interceptors = interceptors;
    }

    public Map<String, Map<String, String>> getInterceptorField() {
        return interceptorField;
    }

    public void setInterceptorField(Map<String, Map<String, String>> interceptorField) {
        this.interceptorField = interceptorField;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }
}
