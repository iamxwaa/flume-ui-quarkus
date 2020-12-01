package org.github.toxrink.resource;

import java.io.File;
import java.io.IOException;
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
import org.github.toxrink.model.TemplateInfo;
import org.github.toxrink.utils.CommonUtils;
import org.github.toxrink.utils.PageUtils;
import org.github.toxrink.utils.TemplateUtils;
import org.jboss.resteasy.annotations.jaxrs.FormParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import lombok.extern.log4j.Log4j2;
import x.utils.TimeUtils;

@Path("template")
@Log4j2
public class TemplateResource {
    @Inject
    @ResourcePath("page/template.html")
    Template template;

    @Inject
    @ResourcePath("page/templateForm.html")
    Template templateForm;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance template() {
        return template.data("templateinfo", TemplateUtils.getTemplateInfoList());
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance templateForm() {
        TemplateInfo ti = new TemplateInfo();
        return templateForm.data("ti", ti);
    }

    @GET
    @Path("/update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance templateUpdate(@QueryParam String tid) throws JsonSyntaxException, IOException {
        System.out.println(tid);
        String cp = TemplateUtils.getJsonFilePath(tid);
        TemplateInfo ti = CommonUtils.readFileToObject(cp, TemplateInfo.class);
        return templateForm.data("ti", ti);
    }

    /**
     * 保存模板
     * 
     * @param tid
     *                    模板id
     * @param name
     *                    模板名称
     * @param desc
     *                    模板描述
     * @param setting
     *                    flume配置
     * @return 调整页面
     * @throws JsonSyntaxException
     *                                 json格式错误
     * @throws IOException
     *                                 读写错误
     */
    @POST
    @Path("save")
    public Response save(@FormParam String tid, @FormParam String name, @FormParam String desc,
            @FormParam String setting) throws JsonSyntaxException, IOException {
        if (StringUtils.isNotEmpty(tid)) {
            Optional<TemplateInfo> ti = TemplateUtils.getTemplateInfoById(tid);
            if (ti.isPresent()) {
                ti.get().setName(name);
                ti.get().setDesc(desc);
                ti.get().setSetting(setting);
                ti.get().setUpdateTime(TimeUtils.format2(TimeUtils.getNow()));
                TemplateUtils.update(ti.get());
                PageUtils.writeInfo("修改模板成功");
            } else {
                log.error("does not exist template id " + tid);
                PageUtils.writeInfo("修改模板ID: " + tid + " 不存在");
            }
        } else {
            TemplateInfo ti = new TemplateInfo();
            ti.setName(name);
            ti.setDesc(desc);
            ti.setSetting(setting);
            ti.setCreateTime(TimeUtils.format2(TimeUtils.getNow()));
            ti.setUpdateTime(TimeUtils.format2(TimeUtils.getNow()));
            TemplateUtils.save(ti);
            PageUtils.writeInfo("新建模板成功");
        }
        return PageUtils.redirect("/template");
    }

    /**
     * 删除模板
     * 
     * @param tid
     *                模板id
     * @return 跳转页面
     * @throws JsonSyntaxException
     *                                 json格式错误
     * @throws IOException
     *                                 读写错误
     */
    @GET
    @Path("/delete")
    public Response delete(@QueryParam String tid) throws JsonSyntaxException, IOException {
        Optional<TemplateInfo> ti = TemplateUtils.getTemplateInfoById(tid);
        if (ti.isPresent()) {
            log.info("delete file " + ti.get().getJsonFilePath());
            FileUtils.forceDelete(new File(ti.get().getJsonFilePath()));
            PageUtils.writeInfo("模板删除成功");
            return PageUtils.redirect("/template");
        }
        log.error("does not exist collect id " + tid);
        PageUtils.writeInfo("修改模板ID: " + tid + " 不存在");
        return PageUtils.redirect("/template");
    }

}
