package org.github.toxrink.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class FileInfo implements Serializable {

    private static final long serialVersionUID = 5500469111411819371L;

    private String name;

    private String path;

    private String createTime;

    private String content;

    private String rpath;

    public String getPath() {
        return toLinux(path);
    }

    public void setPath(String path) {
        this.path = toLinux(path);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    String toLinux(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        return path.replace("\\", "/");
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRpath() {
        return rpath;
    }

    public void setRpath(String rpath) {
        this.rpath = rpath;
    }
}
