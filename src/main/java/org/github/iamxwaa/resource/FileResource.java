package org.github.iamxwaa.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.github.iamxwaa.utils.CommonUtils;
import org.github.iamxwaa.utils.EnvUtils;
import org.github.iamxwaa.utils.PageUtils;
import org.github.iamxwaa.utils.UploadUtils;
import org.jboss.resteasy.annotations.jaxrs.FormParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import lombok.extern.log4j.Log4j2;
import x.utils.TimeUtils;

@Path("file")
@Log4j2
public class FileResource extends StreamResource {
    @Inject
    @ResourcePath("page/file.html")
    Template file;

    @Inject
    @ResourcePath("page/fileForm.html")
    Template fileForm;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance file() {
        return file.data("fileinfo", UploadUtils.getUploadInfoList());
    }

    @GET
    @Path("update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance fileUpdate(@QueryParam String name) throws IOException {
        return fileForm.data("fi", UploadUtils.getFileInfoByPath(name));
    }

    /**
     * 文件上传
     *
     * @param mfile
     *                    上传文件
     * @param request
     *                    上传请求
     * @return 跳转页面
     */
    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@MultipartForm MultipartFormDataInput multipartFormDataInput) {
        AtomicBoolean mark = new AtomicBoolean(true);
        getMultipartBodys(multipartFormDataInput).forEach(multipartBody -> {
            if (!mark.get()) {
                return;
            }
            File file = new File(UploadUtils.getStorePath(multipartBody.getFileName()));
            mark.set(true);
            try (FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(multipartBody.getFile(), output);
                log.info("upload file to " + file.getAbsolutePath());
            } catch (IOException e) {
                log.error("", e);
                mark.set(false);
            }
        });
        PageUtils.writeInfo("文件上传" + (mark.get() ? "成功" : "失败"));
        return PageUtils.redirect("/file");
    }

    /**
     * 删除文件
     *
     * @param name
     *                 文件名称
     * @return 跳转页面
     */
    @GET
    @Path("delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@QueryParam String name) {
        StringBuilder filePath = new StringBuilder(EnvUtils.getFilePath());
        filePath.append("/" + name);
        File file = new File(filePath.toString());
        boolean mark = true;
        if (file.exists()) {
            mark = file.delete();
            log.info("delete file " + file.getAbsolutePath() + " [" + mark + "]");
        }
        PageUtils.writeInfo("文件删除" + (mark ? "成功" : "失败"));
        return PageUtils.redirect("/file");
    }

    /**
     * jar包上传
     *
     * @param mfile
     *                    上传文件
     * @param request
     *                    上传请求
     * @return 跳转页面
     */
    @POST
    @Path("upload/jar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadJar(@MultipartForm MultipartFormDataInput multipartFormDataInput) {
        AtomicBoolean mark = new AtomicBoolean(true);
        getMultipartBodys(multipartFormDataInput).forEach(m -> {
            String dstFile = EnvUtils.getBaseHomePath() + "/flume/" + UploadUtils.FLUME_JAR_DIR + m.getFileName();
            if (!m.getFileName().endsWith(".jar")) {
                PageUtils.writeInfo("文件上传失败,请上传jar文件");
                mark.set(false);
                return;
            }
            File file = new File(dstFile);
            if (file.exists()) {
                String dstFile2 = EnvUtils.getBaseHomePath() + "/flume/" + UploadUtils.FLUME_JAR_DIR + m.getFileName()
                        + "." + TimeUtils.getTimestamp();
                File dest = new File(dstFile2);
                file.renameTo(dest);
                log.info("目标文件已存在,备份为: " + dstFile2);
            }
            file = new File(dstFile);
            try (FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(m.getFile(), output);
                log.info("upload file to " + file.getAbsolutePath());
            } catch (IOException e) {
                log.error("", e);
                mark.set(false);
            }
        });
        PageUtils.writeInfo("文件上传" + (mark.get() ? "成功" : "失败"));
        return PageUtils.redirect("/flume");
    }

    /**
     * 删除jar
     * 
     * @param name
     *                 jar名称
     * @return 跳转页面
     */
    @GET
    @Path("delete/jar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteJar(@QueryParam String name) {
        String dstFile = EnvUtils.getBaseHomePath() + "/flume/" + UploadUtils.FLUME_JAR_DIR + name;
        File file = new File(dstFile);
        boolean mark = true;
        if (file.exists()) {
            mark = file.delete();
            log.info("delete file " + file.getAbsolutePath() + " [" + mark + "]");
        }
        PageUtils.writeInfo("文件删除" + (mark ? "成功" : "失败"));
        return PageUtils.redirect("/flume");
    }

    /**
     * 保存文件修改
     * 
     * @param path
     *                    文件路径
     * @param content
     *                    修改内容
     * @return 跳转页面
     * @throws IOException
     *                         读写异常
     */
    @POST
    @Path("save")
    public Response update(@FormParam String path, @FormParam String content) throws IOException {
        StringBuilder filePath = new StringBuilder(EnvUtils.getFilePath());
        filePath.append("/" + path);
        File file = new File(filePath.toString());

        boolean mark = false;
        if (file.exists()) {
            // 备份文件
            CommonUtils.backupFile(file, EnvUtils.getHistoryFilePath() + "/" + path);
            CommonUtils.writeFile(file, content);
            mark = true;
            log.info("update file " + file.getAbsolutePath());
        }
        PageUtils.writeInfo("文件修改" + (mark ? "成功" : "失败"));
        return PageUtils.redirect("/file");
    }

    /**
     * 日志配置修改
     * 
     * @param content
     *                    修改内容
     * @return 跳转页面
     * @throws IOException
     *                         读写异常
     */
    @POST
    @Path("log4j/save")
    public Response updateLog4j(@FormParam String content) throws IOException {
        File file = new File(UploadUtils.getLog4j().getPath());
        boolean mark = false;
        if (file.exists()) {
            CommonUtils.writeFile(file, content);
            mark = true;
            log.info("update file " + file.getAbsolutePath());
        }
        PageUtils.writeInfo("文件修改" + (mark ? "成功" : "失败"));
        return PageUtils.redirect("/flume#log4j");
    }

    /**
     * 文件下载
     * 
     * @param name
     *                 文件名
     * @param resp
     *                 返回请求
     */
    @GET
    @Path("download")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadFile(@QueryParam String name, @Context HttpServletResponse resp) {
        StringBuilder filePath = new StringBuilder(EnvUtils.getFilePath());
        filePath.append("/" + name);
        File file = new File(filePath.toString());
        log.info("download file " + file.getAbsolutePath());
        download(file, resp);
    }

    /**
     * jar下载
     * 
     * @param name
     *                 jar名称
     * @param resp
     *                 返回请求
     */
    @GET
    @Path("download/jar")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadJarFile(@QueryParam String name, @Context HttpServletResponse resp) {
        String dstFile = EnvUtils.getBaseHomePath() + "/flume/plugins.d/custom/lib/" + name;
        File file = new File(dstFile);
        log.info("download file " + file.getAbsolutePath());
        download(file, resp);
    }

    /**
     * datafix文件生成
     * 
     * @param datafixName
     *                           文件名
     * @param datafixContent
     *                           文件内容
     * @return
     */
    @POST
    @Path("datafix/save")
    public Response saveDatafix(@FormParam String datafixName, @FormParam String datafixContent) {
        File file = new File(UploadUtils.getStorePath(datafixName));
        boolean mark = true;
        try (FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.write(datafixContent.getBytes(), output);
            log.info("upload file to " + file.getAbsolutePath());
        } catch (IOException e) {
            log.error("", e);
            mark = false;
        }
        PageUtils.writeInfo("datafix文件生成" + (mark ? "成功" : "失败"));
        return PageUtils.redirect("/file");
    }

}
