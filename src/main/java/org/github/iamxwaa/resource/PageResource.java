package org.github.iamxwaa.resource;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.github.iamxwaa.config.TouristConfig;
import org.github.iamxwaa.model.JVMInfo;
import org.github.iamxwaa.utils.CollectUtils;
import org.github.iamxwaa.utils.ServerUtils;
import org.github.iamxwaa.utils.UploadUtils;
import org.github.iamxwaa.watcher.CollectorWatcher;
import org.github.iamxwaa.watcher.FileWatcher;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import lombok.experimental.PackagePrivate;

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
    @ResourcePath("page/datafixFile.html")
    Template datafixFile;

    @Inject
    @ResourcePath("page/flume.html")
    Template flume;

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
    @PackagePrivate
    TouristConfig touristConfig;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return index.instance();
    }

    @GET
    @Path("info")
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
    @Path("state")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance state() {
        return state.data("flumeinfo", ServerUtils.getFlumeInfoList());
    }

    @GET
    @Path("tourist")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance tourist() {
        return tourist.data("sources", touristConfig.getSourceMap()).data("channels", touristConfig.getChannelMap())
                .data("sinks", touristConfig.getSinkMap()).data("interceptors", touristConfig.getInterceptorMap());
    }

    @GET
    @Path("setting/datafix")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance datafixFile() {
        return datafixFile.instance();
    }

    @GET
    @Path("flume")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance flume() throws IOException {
        return flume.data("fileinfo", UploadUtils.getFlumeJarList()).data("fi", UploadUtils.getLog4j());
    }

    @GET
    @Path("filewatcher")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance filewatcher() {
        return filewatcher.data("fileinfo", FileWatcher.getUsingFileList());
    }

    @GET
    @Path("collectorwatcher")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance collectorwatcher() {
        return collectorwatcher.data("status", CollectorWatcher.getAutoRestartMap().values());
    }

    @GET
    @Path("test/js")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance js() {
        return js.instance();
    }

    @GET
    @Path("test/datafix")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance datafix() {
        return datafix.instance();
    }

}
