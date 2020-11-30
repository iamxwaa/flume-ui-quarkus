package org.github.toxrink.resource;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonSyntaxException;

import org.github.toxrink.config.TouristConfig;
import org.github.toxrink.model.CollectInfo;
import org.github.toxrink.model.JVMInfo;
import org.github.toxrink.model.TemplateInfo;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.CommonUtils;
import org.github.toxrink.utils.ServerUtils;
import org.github.toxrink.utils.TemplateUtils;
import org.github.toxrink.utils.UploadUtils;
import org.github.toxrink.watcher.CollectorWatcher;
import org.github.toxrink.watcher.FileWatcher;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;

@Path("")
public class PageResource {

    @Inject
    Template index;

    @Inject
    @ResourcePath("page/info.html")
    Template info;

    @Inject
    @ResourcePath("page/state.html")
    Template state;

    @Inject
    @ResourcePath("page/tourist.html")
    Template tourist;

    @Inject
    @ResourcePath("page/template.html")
    Template template;

    @Inject
    @ResourcePath("page/templateForm.html")
    Template templateForm;

    @Inject
    @ResourcePath("page/datafixFile.html")
    Template datafixFile;

    @Inject
    @ResourcePath("page/flume.html")
    Template flume;

    @Inject
    @ResourcePath("page/file.html")
    Template file;

    @Inject
    @ResourcePath("page/filewatcher.html")
    Template filewatcher;

    @Inject
    @ResourcePath("page/collectorwatcher.html")
    Template collectorwatcher;

    @Inject
    @ResourcePath("page/js.html")
    Template js;

    @Inject
    @ResourcePath("page/datafix.html")
    Template datafix;

    @Inject
    @ResourcePath("page/fileForm.html")
    Template fileForm;

    @Inject
    private TouristConfig touristConfig;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return index.instance();
    }

    @GET
    @Path("/info")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance info() {
        JVMInfo jvm = new JVMInfo();
        int totalCount = ServerUtils.getFlumeInfoList().size();
        int runCount = ServerUtils.getRunningFlumeInfoList().size();
        int restartCount = CollectUtils.getRestartCount();
        int stopCount = totalCount - runCount - restartCount;
        return info.data("jvm", jvm).data("totalCount", totalCount).data("runCount", runCount)
                .data("restartCount", restartCount).data("stopCount", stopCount);
    }

    @GET
    @Path("/state")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance state() {
        return state.data("flumeinfo", ServerUtils.getFlumeInfoList());
    }

    @GET
    @Path("/tourist")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance tourist() {
        return tourist.data("sources", touristConfig.getSourceMap()).data("channels", touristConfig.getChannelMap())
                .data("sinks", touristConfig.getSinkMap()).data("interceptors", touristConfig.getInterceptorMap());
    }

    @GET
    @Path("/template")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance template() {
        return template.data("templateinfo", TemplateUtils.getTemplateInfoList());
    }

    @GET
    @Path("/template/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance templateForm() {
        TemplateInfo ti = new TemplateInfo();
        return templateForm.data("ti", ti);
    }

    @GET
    @Path("/template/update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance templateUpdate(@QueryParam String tid) throws JsonSyntaxException, IOException {
        System.out.println(tid);
        String cp = TemplateUtils.getJsonFilePath(tid);
        TemplateInfo ti = CommonUtils.readFileToObject(cp, TemplateInfo.class);
        return templateForm.data("ti", ti);
    }

    @GET
    @Path("/setting/datafix")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance datafixFile() {
        return datafixFile.instance();
    }

    @GET
    @Path("/flume")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance flume() throws IOException {
        return flume.data("fileinfo", UploadUtils.getFlumeJarList()).data("fi", UploadUtils.getLog4j());
    }

    @GET
    @Path("/file")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance file() {
        return file.data("fileinfo", UploadUtils.getUploadInfoList());
    }

    @GET
    @Path("/filewatcher")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance filewatcher() {
        return filewatcher.data("fileinfo", FileWatcher.getUseingFileList());
    }

    @GET
    @Path("/collectorwatcher")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance collectorwatcher() {
        return collectorwatcher.data("status", CollectorWatcher.getAutoRestartMap().values());
    }

    @GET
    @Path("/test/js")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance js() {
        return js.instance();
    }

    @GET
    @Path("/test/datafix")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance datafix() {
        return datafix.instance();
    }

    @GET
    @Path("/file/update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance fileUpdate(@QueryParam String name) throws IOException {
        return fileForm.data("fi", UploadUtils.getFileInfoByPath(name));
    }

}
