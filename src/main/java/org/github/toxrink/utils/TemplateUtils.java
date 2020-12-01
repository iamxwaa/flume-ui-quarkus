package org.github.toxrink.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.github.toxrink.model.TemplateInfo;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class TemplateUtils {
    /**
     * 获取采集器信息
     * 
     * @return 模板对象列表
     */
    public static List<TemplateInfo> getTemplateInfoList() {
        String path = EnvUtils.getTemplatePath();
        return CommonUtils.sort(Arrays.asList(new File(path).listFiles()).stream().map(f -> {
            if (f.getName().endsWith(".json")) {
                TemplateInfo ti = CommonUtils.readFileToObject(f, TemplateInfo.class);
                return ti;
            } else {
                log.warn("ignore template : " + f.getAbsolutePath());
            }
            return null;
        }).filter(f -> f != null).collect(Collectors.toList()));
    }

    /**
     * 保存采集器信息
     * 
     * @param ti
     *               模板对象
     * @throws IOException
     *                         写错误
     */
    public static void save(TemplateInfo ti) throws IOException {
        fillTemplateInfo(ti, true);
        log.info("save template to " + ti.getJsonFilePath());
        CommonUtils.writeFile(ti.getJsonFilePath(), ti);
    }

    /**
     * 获取模板对象
     * 
     * @param tid
     *                模板id
     * @return 模板对象
     * @throws JsonSyntaxException
     *                                 json格式错误
     * @throws IOException
     *                                 读错误
     */
    public static Optional<TemplateInfo> getTemplateInfoById(String tid) throws JsonSyntaxException, IOException {
        if (StringUtils.isEmpty(tid)) {
            return Optional.empty();
        }
        String jsonFilePath = getJsonFilePath(tid);
        TemplateInfo ti = CommonUtils.readFileToObject(jsonFilePath, TemplateInfo.class);
        ti.setJsonFilePath(jsonFilePath);
        return Optional.of(ti);
    }

    /**
     * 修改采集器信息
     * 
     * @param ti
     *               模板对象
     * @throws IOException
     *                         写异常
     */
    public static void update(TemplateInfo ti) throws IOException {
        fillTemplateInfo(ti, false);
        log.info("update template : " + ti.getJsonFilePath());
        CommonUtils.writeFile(ti.getJsonFilePath(), ti);
    }

    /**
     * 补全CollectInfo 必要字段
     * 
     * @param ti
     *                   模板对象
     * @param autoId
     *                   是否自动生成模板id
     */
    public static void fillTemplateInfo(TemplateInfo ti, boolean autoId) {
        if (StringUtils.isEmpty(ti.getId()) && !autoId) {
            return;
        }
        if (autoId) {
            ti.setId(getTemplateId(ti));
        }
        if (StringUtils.isEmpty(ti.getJsonFilePath())) {
            ti.setJsonFilePath(getJsonFilePath(ti.getId()));
        }
    }

    /**
     * 获取采集器信息文件名
     * 
     * @param tid
     *                模板id
     * @return
     */
    public static String getJsonFilePath(String tid) {
        String path = EnvUtils.getTemplatePath();
        return path + "/" + tid + ".json";
    }

    /**
     * 获取采集器信息文件名
     * 
     * @param ti
     *               TemplateInfo
     * @return
     */
    public static String getTemplateId(TemplateInfo ti) {
        return CommonUtils.string2MD5(ti.getName() + ti.getDesc() + System.currentTimeMillis());
    }
}
