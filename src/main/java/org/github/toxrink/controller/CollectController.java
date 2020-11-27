package org.github.toxrink.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.model.CollectInfo;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.PageAlertUtils;
import org.github.toxrink.utils.ServerUtils;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import x.utils.TimeUtils;

public class CollectController {
    private static final Log LOG = LogFactory.getLog(CollectController.class);

    /**
     * 保存采集器
     * 
     * @param cid
     *                           采集器id
     * @param name
     *                           名称
     * @param desc
     *                           描述
     * @param company
     *                           公司
     * @param product
     *                           产品
     * @param productVersion
     *                           产品版本
     * @param setting
     *                           flume配置
     * @param memSize
     *                           内存大小
     * @return 跳转页面
     * @throws JsonSyntaxException
     *                                 json格式错误
     * @throws IOException
     *                                 读写异常
     */
    @POST
    @Path("/collect/save")
    @Produces(MediaType.TEXT_HTML)
    public String save(@QueryParam String cid, @QueryParam String name, @QueryParam String desc,
            @QueryParam String company, @QueryParam String product, @QueryParam String productVersion,
            @QueryParam String setting, @QueryParam String memSize, @QueryParam String autoStart,
            @QueryParam String autoRestart) throws JsonSyntaxException, IOException {
        if (StringUtils.isNotEmpty(cid)) {
            Optional<CollectInfo> ci = CollectUtils.getCollectInfoById(cid);
            if (ci.isPresent()) {
                ci.get().setName(name);
                ci.get().setCompany(company);
                ci.get().setProduct(product);
                ci.get().setProductVersion(productVersion);
                ci.get().setDesc(desc);
                ci.get().setSetting(setting);
                ci.get().setUpdateTime(TimeUtils.format2(TimeUtils.getNow()));
                if (null != autoStart) {
                    ci.get().setAutoStart(autoStart.toLowerCase());
                } else {
                    ci.get().setAutoStart(null);
                }
                if (null != autoRestart) {
                    ci.get().setAutoRestart(autoRestart.toLowerCase());
                } else {
                    ci.get().setAutoRestart(null);
                }
                if (StringUtils.isNumeric(memSize)) {
                    ci.get().setMemSize(memSize);
                } else {
                    ci.get().setMemSize("2048");
                }
                CollectUtils.update(ci.get());
                PageAlertUtils.writeInfo("修改采集器成功");
            } else {
                LOG.error("does not exist collect id " + cid);
                PageAlertUtils.writeInfo("修改采集器ID: " + cid + " 不存在");
            }
        } else {
            CollectInfo ci = new CollectInfo();
            ci.setName(name);
            ci.setCompany(company);
            ci.setProduct(product);
            ci.setProductVersion(productVersion);
            ci.setDesc(desc);
            ci.setSetting(setting);
            ci.setCreateTime(TimeUtils.format2(TimeUtils.getNow()));
            ci.setUpdateTime(ci.getCreateTime());
            if (null != autoStart) {
                ci.setAutoStart(autoStart.toLowerCase());
            } else {
                ci.setAutoStart(null);
            }
            if (null != autoRestart) {
                ci.setAutoRestart(autoRestart.toLowerCase());
            } else {
                ci.setAutoRestart(null);
            }
            if (StringUtils.isNumeric(memSize)) {
                ci.setMemSize(memSize);
            } else {
                ci.setMemSize("2048");
            }
            CollectUtils.save(ci);
            PageAlertUtils.writeInfo("新建采集器成功");
        }
        return "redirect:/collect";
    }

    /**
     * 删除采集器
     * 
     * @param cid
     *                采集器id
     * @return 跳转页面
     * @throws IOException
     *                         读写异常
     */
    @GET
    @Path("/collect/delete")
    @Produces(MediaType.TEXT_HTML)
    public String delete(@QueryParam String cid) throws IOException {
        CollectUtils.stop(cid);
        if (!ServerUtils.getRunningFlumeInfoById(cid).isPresent()) {
            Optional<CollectInfo> ci = CollectUtils.getCollectInfoById(cid);
            if (ci.isPresent()) {
                LOG.info("delete file " + ci.get().getConfFilePath());
                FileUtils.forceDelete(new File(ci.get().getConfFilePath()));
                LOG.info("delete file " + ci.get().getJsonFilePath());
                FileUtils.forceDelete(new File(ci.get().getJsonFilePath()));
            } else {
                LOG.error("does not exist collect id " + cid);
                PageAlertUtils.writeInfo("修改采集器ID: " + cid + " 不存在");
            }
            PageAlertUtils.writeInfo("采集器删除成功");
        } else {
            PageAlertUtils.writeInfo("采集器删除失败[运行中]");
        }
        return "redirect:/collect";
    }

    /**
     * 启动采集器
     *
     * @param cid
     *                采集器id
     * @return 跳转页面
     * @throws JsonSyntaxException
     *                                 json格式错误
     * @throws IOException
     *                                 读写异常
     */
    @GET
    @Path("/collect/start")
    @Produces(MediaType.TEXT_HTML)
    public String start(@QueryParam String cid) throws JsonSyntaxException, IOException {
        Optional<String> m = CollectUtils.start(cid);
        if (m.isPresent()) {
            PageAlertUtils.writeInfo(m.get());
        } else {
            PageAlertUtils.writeInfo("采集器启动成功");
        }
        return "redirect:/state";
    }

    /**
     * 关闭采集器
     * 
     * @param cid
     *                采集器id
     * @return
     */
    @GET
    @Path("/collect/stop")
    @Produces(MediaType.TEXT_HTML)
    public String stop(@QueryParam String cid) {
        Optional<String> m = CollectUtils.stop(cid);
        if (m.isPresent()) {
            PageAlertUtils.writeInfo(m.get());
        } else {
            PageAlertUtils.writeInfo("采集器停止成功");
        }
        return "redirect:/state";
    }
}
