package org.github.toxrink.model;

public class TemplateInfo extends FileInfo {

    private static final long serialVersionUID = 4564708398625091187L;

    private String id;

    private String desc;

    private String updateTime;

    private String jsonFilePath;

    private String setting;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonFilePath() {
        return toLinux(jsonFilePath);
    }

    public void setJsonFilePath(String jsonFilePath) {
        this.jsonFilePath = toLinux(jsonFilePath);
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}
