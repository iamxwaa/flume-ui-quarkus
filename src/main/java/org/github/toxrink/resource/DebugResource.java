package org.github.toxrink.resource;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.github.toxrink.utils.EnvUtils;
import org.github.toxrink.utils.ServerUtils;
import org.jboss.resteasy.annotations.jaxrs.FormParam;

import lombok.extern.log4j.Log4j2;
import x.os.CmdWrapper;
import x.os.JxProcess;

@Path("debug")
@Log4j2
public class DebugResource {
    /**
     * js测试
     * 
     * @param headerdata
     *                       头信息
     * @param jscontent
     *                       js脚本
     * @param jspath
     *                       js路径
     * @param sourcedata
     *                       测试数据
     * @return
     */
    @POST
    @Path("js")
    @Produces(MediaType.APPLICATION_JSON)
    public String testJS(@FormParam String headerdata, @FormParam String jscontent, @FormParam String jspath,
            @FormParam String sourcedata) {
        String path = jspath;
        if (StringUtils.isEmpty(path)) {
            String jsName = "test-" + System.currentTimeMillis();
            File testJS = new File(FileUtils.getTempDirectory(), jsName);
            try {
                FileUtils.writeStringToFile(testJS, jscontent, EnvUtils.UTF8);
            } catch (IOException e) {
                log.error("", e);
                return "创建临时JS文件失败";
            }
            path = testJS.getAbsolutePath();
        }
        String run = EnvUtils.getBaseHomePath() + "/flume/tools/vap-flume-tools";
        String p = "-p" + path;
        String d = "-d" + sourcedata;
        String h = "-h" + headerdata;
        String[] command = new String[] { "sh", run, "js", p, d, h };
        if (ServerUtils.isWindows()) {
            run = EnvUtils.getBaseHomePath() + "/flume/tools/vap-flume-tools.bat";
            command = new String[] { run, "js", p, d, h };
        }
        JxProcess process = CmdWrapper.runWithEnv(command);
        String in = getInputStreamAsString(process);
        process.destroy();
        return in;
    }

    /**
     * datafix测试
     * 
     * @param inputType
     *                      类型
     * @param datafix
     *                      路径
     * @param area
     *                      路径
     * @param domain
     *                      路径
     * @param filter
     *                      路径
     * @param tables
     *                      路径
     * @param head
     *                      头信息
     * @param body
     *                      数据
     * @return
     */
    @POST
    @Path("datafix")
    @Produces(MediaType.APPLICATION_JSON)
    public String testJS(@FormParam String inputType, @FormParam String datafix, @FormParam String area,
            @FormParam String domain, @FormParam String filter, @FormParam String tables, @FormParam String head,
            @FormParam String body) {
        String run = EnvUtils.getBaseHomePath() + "/flume/tools/vap-flume-tools";
        String i = "-i" + inputType;
        String b = "-b" + body;
        String h = "-h" + head;
        String d = "-d" + datafix;
        String a = "-a" + area;
        String u = "-u" + domain;
        String f = "-f" + filter;
        String t = "-t" + tables;
        String[] command = new String[] { "sh", run, "runDatafix", i, b, h, d, a, u, f, t };
        if (ServerUtils.isWindows()) {
            run = EnvUtils.getBaseHomePath() + "/flume/tools/vap-flume-tools.bat";
            command = new String[] { run, "runDatafix", i, b, h, d, a, u, f, t };
        }
        JxProcess process = CmdWrapper.runWithEnv(command);
        String in = getInputStreamAsString(process);
        process.destroy();
        return in;
    }

    private String getInputStreamAsString(JxProcess process) {
        String in = null;
        if (ServerUtils.isWindows()) {
            in = process.toStringFromInput("GBK");
            if (StringUtils.isEmpty(in)) {
                in = process.toStringFromError("GBK");
            }
        } else {
            in = process.toStringFromInput("UTF-8");
            if (StringUtils.isEmpty(in)) {
                in = process.toStringFromError("UTF-8");
            }
        }
        return in;
    }
}