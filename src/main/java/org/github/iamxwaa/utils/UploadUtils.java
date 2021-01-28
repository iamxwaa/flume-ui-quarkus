package org.github.iamxwaa.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import x.utils.TimeUtils;

import org.github.iamxwaa.config.EnvConfig;
import org.github.iamxwaa.model.FileInfo;

public final class UploadUtils {
    /**
     * 子文件夹个数
     */
    public static final int SUB_COUNT = 32;

    /**
     * 自定义flume jar包目录
     */
    public static final String FLUME_JAR_DIR = "plugins.d/custom/lib/";

    /**
     * 获取上传文件信息
     */
    public static List<FileInfo> getUploadInfoList() {
        List<FileInfo> list = new ArrayList<>();
        for (File f : FileUtils.listFiles(new File(EnvUtils.getFilePath()), null, true)) {
            FileInfo u = buildFileInfo(f, false);
            if (u != null) {
                list.add(u);
            }
        }
        return CommonUtils.sort(list);
    }

    /**
     * 获取自定义jar包信息
     */
    public static List<FileInfo> getFlumeJarList() {
        List<FileInfo> list = new ArrayList<>();
        for (File f : FileUtils.listFiles(new File(EnvUtils.getBaseHomePath() + "/flume/" + FLUME_JAR_DIR), null,
                true)) {
            FileInfo u = buildFileInfo(f, false);
            if (u != null) {
                list.add(u);
            }
        }
        return CommonUtils.sort(list);
    }

    /**
     * 获取文件信息
     * 
     * @param path
     *                 文件路径
     * @return 文件信息
     * @throws IOException
     *                         读写异常
     */
    public static FileInfo getFileInfoByPath(String path) throws IOException {
        return buildFileInfo(new File(EnvUtils.getFilePath() + path), true);
    }

    /**
     * 获取log4j
     * 
     * @return log4j内容
     * @throws IOException
     *                         读写异常
     */
    public static FileInfo getLog4j() throws IOException {
        EnvConfig config = EnvUtils.getEnvConfig();
        String path = EnvUtils.getBaseHomePath() + "/flume/conf/log4j.properties";
        if (config.isAmbari()) {
            path = EnvUtils.getAmbariPath() + "/log4j.properties";
        }
        return buildFileInfo(new File(path), true);
    }

    /**
     * 构建 FileInfo
     * 
     * @param file
     *                       文件
     * @param getContent
     *                       是否读取文件内容
     * @return FileInfo
     */
    public static FileInfo buildFileInfo(File file, boolean getContent) {
        if (!file.exists()) {
            return null;
        }
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(file.getName());
        fileInfo.setPath(file.getAbsolutePath());
        fileInfo.setCreateTime(TimeUtils.format2(new Date(file.lastModified())));
        if (getContent) {
            fileInfo.setContent(CommonUtils.readFileToString(file));
        }
        fileInfo.setRpath(fileInfo.getPath().substring(EnvUtils.getFilePath().length()));
        if (fileInfo.getRpath().charAt(0) != '/') {
            fileInfo.setRpath('/' + fileInfo.getRpath());
        }
        return fileInfo;
    }

    /**
     * 创建子文件夹
     */
    public static void createSubDir() {
        for (int i = 0; i < SUB_COUNT; i++) {
            String path = EnvUtils.getFilePath();
            String dir = Integer.toHexString(i);
            if (dir.length() == 1) {
                dir = "0" + dir;
            }
            File file = new File(path + File.separator + dir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    /**
     * 获取存储路径
     * 
     * @param fileName
     *                     文件名
     * @return 存储路径
     */
    public static String getStorePath(String fileName) {
        String path = null;
        for (int i = 0; i < SUB_COUNT; i++) {
            path = EnvUtils.getFilePath();
            String dir = Integer.toHexString(i);
            if (dir.length() == 1) {
                dir = "0" + dir;
            }
            path = path + File.separator + dir + File.separator + fileName;
            File file = new File(path);
            if (!file.exists()) {
                return path;
            }
        }
        StringBuilder filePath = new StringBuilder(path);
        int i = filePath.lastIndexOf(".");
        if (-1 == i) {
            filePath.append(System.currentTimeMillis());
        } else {
            filePath.insert(i, "_" + System.currentTimeMillis());
        }
        return filePath.toString();
    }
}
