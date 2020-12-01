package org.github.toxrink.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.model.ConfigSettingInfo;
import org.github.toxrink.model.FileInfo;

import x.utils.StrUtils;
import x.utils.TimeUtils;

/**
 * 通用工具
 * 
 * @author xw
 * 
 *         2019年04月22日
 */
public class CommonUtils {
    private static final Log LOG = LogFactory.getLog(CommonUtils.class);

    /**
     * 生成32位MD5
     * 
     * @param inStr
     *                  数据
     * @return
     */
    public static String string2MD5(String inStr) {
        return StrUtils.toMD5(inStr);
    }

    /**
     * 校验是否授权
     * 
     * @param token
     *                    认证信息
     * @param authUrl
     *                    认证链接
     * @return 认证结果
     */
    public static boolean checkAuthorization(String token, String authUrl) {
        try {
            URL url = new URL(authUrl);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(30 * 1000);
            conn.setReadTimeout(30 * 1000);
            conn.setDoOutput(true);
            conn.setDoInput(false);
            conn.connect();
            conn.getOutputStream().write(
                    new StringBuilder().append("{\"token\":\"").append(token).append("\"}").toString().getBytes());
            boolean auth = IOUtils.readLines(conn.getInputStream(), EnvUtils.UTF8).stream().anyMatch(s -> {
                return s.contains("\"res\":true");
            });
            return auth;
        } catch (IOException e) {
            LOG.error("", e);
        }
        return false;
    }

    /**
     * 时间排序
     * 
     * @param list
     *                 排序的list
     * @return 排序后的list
     */
    public static <T extends FileInfo> List<T> sort(List<T> list) {
        if (null == list) {
            return new ArrayList<>(0);
        }
        list.sort(sortFileInfo());
        return (List<T>) list;
    }

    /**
     * 时间排序
     * 
     * @return 排序类
     */
    public static Comparator<FileInfo> sortFileInfo() {
        return (a, b) -> {
            long t1 = TimeUtils.parese2(a.getCreateTime()).getTime();
            long t2 = TimeUtils.parese2(b.getCreateTime()).getTime();
            return (t1 > t2) ? -1 : (t1 == t2 ? 0 : 1);
        };

    }

    /**
     * 写json对象
     * 
     * @param path
     *                 文件路径
     * @param obj
     *                 写入对象
     */
    public static void writeFile(String path, FileInfo obj) {
        writeFile(path, new Gson().toJson(obj));
    }

    /**
     * 写文件
     * 
     * @param path
     *                    文件路径
     * @param content
     *                    写入内容
     */
    public static void writeFile(String path, String content) {
        writeFile(new File(path), content);
    }

    /**
     * 写文件
     * 
     * @param file
     *                    文件
     * @param content
     *                    写入内容
     */
    public static void writeFile(File file, String content) {
        try {
            FileUtils.write(file, content, EnvUtils.UTF8);
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    /**
     * 备份文件
     *
     * @param path
     *                 文件路径
     */
    public static void backupFile(String path) {
        try {
            FileUtils.copyFile(new File(path), new File(path + ".bak"));
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    /**
     * 文件管理备份历史修改文件
     *
     * @param sourceFile
     *                       源文件
     * @param targetPath
     *                       文件路径
     */
    public static void backupFile(File sourceFile, String targetPath) {
        StringBuilder sb = new StringBuilder(targetPath);
        String fileName = sourceFile.getName();
        sb.append("/").append(TimeUtils.getTimestamp()).append("_").append(fileName);
        try {
            FileUtils.copyFile(sourceFile, new File(sb.toString()));
        } catch (IOException e) {
            LOG.error("failed for backup", e);
        }
    }

    /**
     * 读文件
     * 
     * @param path
     *                  文件路径
     * @param clazz
     *                  返回类
     * @return
     */
    public static <T> T readFileToObject(String path, Class<T> clazz) {
        return readFileToObject(new File(path), clazz);
    }

    /**
     * 读文件
     * 
     * @param file
     *                  文件
     * @param clazz
     *                  返回类
     * @return
     */
    public static <T> T readFileToObject(File file, Class<T> clazz) {
        String tmp = readFileToString(file);
        if (StringUtils.isEmpty(tmp)) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.error("", e);
                return null;
            }
        }
        return new Gson().fromJson(tmp, clazz);
    }

    /**
     * 读文件
     * 
     * @param file
     *                 文件路径
     * @return 文件内容
     */
    public static String readFileToString(String file) {
        return readFileToString(new File(file));
    }

    /**
     * 读文件
     * 
     * @param file
     *                 文件
     * @return 文件内容
     */
    public static String readFileToString(File file) {
        if (file.exists()) {
            try {
                return FileUtils.readFileToString(file, EnvUtils.UTF8);
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
        return "";
    }

    /**
     * 将配置文件解析成易解读图形配置的json
     *
     * @param filePath
     *                     解析路径文件
     * @return
     */
    public static String getFileConfigToJson(String filePath) {
        ConfigSettingInfo cs = getConfigInfoForTu(filePath, null);
        return new Gson().toJson(cs).replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\\\\\\\"", "\\\\\"");
    }

    /**
     * 解析配置json
     *
     * @param filePath
     *                     content 为空时解析路径文件
     * @param content
     *                     不为空时直接解析content，不读路径文件
     * @return
     */
    public static ConfigSettingInfo getConfigInfoForTu(String filePath, String content) {
        final Map<String, String> fileConfigToMap;
        if (content != null) {
            fileConfigToMap = getFileConfigToMap2(content);
        } else {
            fileConfigToMap = getFileConfigToMap(filePath);
        }

        ConfigSettingInfo cs = new ConfigSettingInfo();
        if (fileConfigToMap == null) {
            return cs;
        }

        final String[] channels = fileConfigToMap.get("a1.channels").split(" ");
        final String[] sources = fileConfigToMap.get("a1.sources").split(" ");
        final String[] sinks = fileConfigToMap.get("a1.sinks").split(" ");

        Arrays.stream(sources).forEach(r -> {
            String interceptors = fileConfigToMap.get("a1.sources." + r + ".interceptors");
            if (interceptors == null) {
                return;
            }
            ArrayList<String> list = new ArrayList<>(Arrays.asList(interceptors.split(" ")));
            // 解析source和interceptors的对应关系
            cs.getInterceptors().put(r, list);
            for (String s : list) {
                cs.getInterceptorField().put(s, new LinkedHashMap<>());
            }
        });

        Set<String> keySet = fileConfigToMap.keySet();

        cs.setChannels(channels);
        cs.setSources(sources);
        cs.setSinks(sinks);

        // 解析所有channel及其详细配置信息
        for (String channelIndex : channels) {
            Map<String, String> channel = new HashMap<>();
            cs.getChannelsField().put(channelIndex, channel);
            keySet.stream().filter(r -> r.startsWith("a1.channels." + channelIndex)
                    && r.length() > ("a1.channels." + channelIndex).length()).forEach(r -> {
                        String channelField = r.replaceFirst("a1.channels." + channelIndex + ".", "");
                        channel.put(channelField, fileConfigToMap.get(r));
                    });

        }

        // 解析所有source及其详细配置信息(包含Interceptor信息)
        for (String sourceIndex : sources) {
            Map<String, String> channel = new HashMap<>();
            cs.getSourcesField().put(sourceIndex, channel);
            keySet.stream().filter(r -> r.startsWith("a1.sources." + sourceIndex)
                    && r.length() > ("a1.sources." + sourceIndex).length()).forEach(r -> {
                        if (r.startsWith("a1.sources." + sourceIndex + ".interceptors")
                                && r.length() > ("a1.sources." + sourceIndex + ".interceptors").length()) {
                            String interceptorField = r
                                    .replaceFirst("a1.sources." + sourceIndex + ".interceptors" + ".", "");
                            String[] interceptorIndexSplit = interceptorField.split("\\.", 2);
                            cs.getInterceptorField().get(interceptorIndexSplit[0]).put(interceptorIndexSplit[1],
                                    fileConfigToMap.get(r));
                            return;
                        }
                        String channelField = r.replaceFirst("a1.sources." + sourceIndex + ".", "");
                        channel.put(channelField, fileConfigToMap.get(r));
                    });
        }

        // 解析所有sinks及其详细配置信息
        for (String sinkIndex : sinks) {
            Map<String, String> channel = new HashMap<>();
            cs.getSinksField().put(sinkIndex, channel);
            keySet.stream().filter(r -> {
                boolean a = r.startsWith("a1.sinks." + sinkIndex);
                return a && r.length() > ("a1.sinks." + sinkIndex).length();
            }).forEach(r -> {
                String channelField = r.replaceFirst("a1.sinks." + sinkIndex + ".", "");
                channel.put(channelField, fileConfigToMap.get(r));
            });
        }
        return cs;
    }

    protected static Map<String, String> getFileConfigToMap(String filePath) {
        Properties pps = new Properties();
        Map<String, String> map = null;
        try (InputStream in = new BufferedInputStream(new FileInputStream(filePath))) {
            pps.load(new InputStreamReader(in, "UTF-8"));
            map = new LinkedHashMap<>(pps.keySet().size());
            Map<String, String> finalMap = map;
            pps.keySet().forEach(r -> {
                finalMap.put(r.toString(), pps.getProperty(r.toString()));
            });
            return finalMap;
        } catch (IOException e) {
            LOG.error("", e);
            return null;
        }
    }

    protected static Map<String, String> getFileConfigToMap2(String content) {
        Properties pps = new Properties();
        Map<String, String> map = null;
        try (InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));) {
            pps.load(new InputStreamReader(in, "UTF-8"));
            map = new LinkedHashMap<>(pps.keySet().size());
            Map<String, String> finalMap = map;
            pps.keySet().forEach(r -> {
                finalMap.put(r.toString(), pps.getProperty(r.toString()));
            });
            return finalMap;
        } catch (IOException e) {
            LOG.error("", e);
            return null;
        }
    }

}
