package org.github.toxrink.resource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.config.TouristConfig;
import org.github.toxrink.model.CollectInfo;
import org.github.toxrink.model.TemplateInfo;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.CommonUtils;
import org.github.toxrink.utils.PageUtils;
import org.github.toxrink.utils.ServerUtils;
import org.github.toxrink.utils.TemplateUtils;
import org.jboss.resteasy.annotations.jaxrs.FormParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import x.utils.TimeUtils;

@Path("collect")
public class CollectResource {
    private static final Log LOG = LogFactory.getLog(CollectResource.class);

    @Inject
    private TouristConfig touristConfig;

    @Inject
    @ResourcePath("page/collect.html")
    Template collect;

    @Inject
    @ResourcePath("page/collectForm.html")
    Template collectForm;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance collect() {
        return collect.data("collectinfo", CollectUtils.getCollectInfoList());
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance collectNew() {
        CollectInfo ci = new CollectInfo();
        List<TemplateInfo> templates = TemplateUtils.getTemplateInfoList();
        return collectForm.data("ci", ci).data("templates", templates).data("sources", touristConfig.getSourceMap())
                .data("channels", touristConfig.getChannelMap()).data("sinks", touristConfig.getSinkMap())
                .data("interceptors", touristConfig.getInterceptorMap());
    }

    @GET
    @Path("/update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance collectUpdate(@QueryParam String cid) throws JsonSyntaxException, IOException {
        String cp = CollectUtils.getJsonFilePath(cid);
        CollectInfo ci = CommonUtils.readFileToObject(cp, CollectInfo.class);
        List<TemplateInfo> templates = TemplateUtils.getTemplateInfoList();
        return collectForm.data("ci", ci).data("templates", templates).data("sources", touristConfig.getSourceMap())
                .data("channels", touristConfig.getChannelMap()).data("sinks", touristConfig.getSinkMap())
                .data("interceptors", touristConfig.getInterceptorMap());
    }

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
    @Path("/save")
    public Response save(@FormParam String cid, @FormParam String name, @FormParam String desc,
            @FormParam String company, @FormParam String product, @FormParam String productVersion,
            @FormParam String setting, @FormParam String memSize, @FormParam String autoStart,
            @FormParam String autoRestart) throws JsonSyntaxException, IOException {
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
                PageUtils.writeInfo("修改采集器成功");
            } else {
                LOG.error("does not exist collect id " + cid);
                PageUtils.writeInfo("修改采集器ID: " + cid + " 不存在");
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
            PageUtils.writeInfo("新建采集器成功");
        }
        return PageUtils.redirect("/collect");
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
    @Path("/delete")
    public Response delete(@QueryParam String cid) throws IOException {
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
                PageUtils.writeInfo("修改采集器ID: " + cid + " 不存在");
            }
            PageUtils.writeInfo("采集器删除成功");
        } else {
            PageUtils.writeInfo("采集器删除失败[运行中]");
        }
        return PageUtils.redirect("/collect");
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
    @Path("/start")
    public Response start(@QueryParam String cid) throws JsonSyntaxException, IOException {
        Optional<String> m = CollectUtils.start(cid);
        if (m.isPresent()) {
            PageUtils.writeInfo(m.get());
        } else {
            PageUtils.writeInfo("采集器启动成功");
        }
        return PageUtils.redirect("/state");
    }

    /**
     * 关闭采集器
     * 
     * @param cid
     *                采集器id
     * @return
     */
    @GET
    @Path("/stop")
    public Response stop(@QueryParam String cid) {
        Optional<String> m = CollectUtils.stop(cid);
        if (m.isPresent()) {
            PageUtils.writeInfo(m.get());
        } else {
            PageUtils.writeInfo("采集器停止成功");
        }
        return PageUtils.redirect("/state");
    }
}
