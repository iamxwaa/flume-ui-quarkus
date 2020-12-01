package org.github.toxrink.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.github.toxrink.model.MultipartBody;
import org.github.toxrink.model.TemplateInfo;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.EnvUtils;
import org.github.toxrink.utils.PageUtils;
import org.github.toxrink.utils.TemplateUtils;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import lombok.extern.log4j.Log4j2;
import x.os.CmdWrapper;
import x.os.FileInfo;
import x.utils.TimeUtils;

@Path("")
@Log4j2
public class StreamResource {
    /**
     * 模板打包下载
     * 
     * @param resp
     *                 返回请求
     */
    @GET
    @Path("pkg/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void pkgFile(@Context HttpServletResponse resp) {
        try {
            List<TemplateInfo> list = TemplateUtils.getTemplateInfoList();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(list);
            out.close();
            byte[] tmp = out.toByteArray();
            byte[] tmp2 = new byte[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                tmp2[tmp2.length - 1 - i] = (byte) (tmp[i] ^ 111);
            }
            download("templates.pkg", new ByteArrayInputStream(tmp2), resp);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    /**
     * 模板配置导入
     *
     * @param mfile
     *                    上传文件
     * @param request
     *                    上传请求
     * @return
     */
    @POST
    @Path("pkg/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadPkg(@MultipartForm MultipartFormDataInput multipartFormDataInput) {
        getMultipartBodys(multipartFormDataInput).forEach(m -> {
            try {
                byte[] buffer = new byte[1024];
                int len = 0;
                InputStream in = m.getFile();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();
                byte[] tmp = out.toByteArray();
                byte[] tmp2 = new byte[tmp.length];
                for (int i = 0; i < tmp.length; i++) {
                    tmp2[tmp2.length - 1 - i] = (byte) (tmp[i] ^ 111);
                }
                ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(tmp2));
                @SuppressWarnings("unchecked")
                List<TemplateInfo> list = (List<TemplateInfo>) oin.readObject();
                list.forEach(f -> {
                    TemplateInfo ti = new TemplateInfo();
                    ti.setName(f.getName());
                    ti.setDesc(f.getDesc());
                    ti.setSetting(f.getSetting());
                    ti.setCreateTime(TimeUtils.format2(TimeUtils.getNow()));
                    ti.setUpdateTime(TimeUtils.format2(TimeUtils.getNow()));
                    try {
                        TemplateUtils.save(ti);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                });
                PageUtils.writeInfo("模板导入成功");
            } catch (IOException | ClassNotFoundException e) {
                log.error("", e);
            }
        });
        return PageUtils.redirect("/template");
    }

    /**
     * 采集器配置打包
     * 
     * @param cid
     *                 采集器id
     * @param resp
     *                 返回请求
     * @throws IOException
     *                         读写异常
     */
    @GET
    @Path("collect/package")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void downloadPackage(@QueryParam String cid, @Context HttpServletResponse resp) throws IOException {
        List<FileInfo> list = CollectUtils.getCollectFilePath(cid);
        File zfile = CmdWrapper.zip(list, "flume-" + TimeUtils.getTimestamp() + "_");
        download(zfile, resp);
    }

    /**
     * 日志下载
     * 
     * @param cid
     *                 采集器id
     * @param resp
     *                 返回请求
     * @throws IOException
     *                         读写异常
     */
    @GET
    @Path("log/download")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadLog(@QueryParam String cid, @Context HttpServletResponse resp) throws IOException {
        File file = CollectUtils.getLogFileById(cid);
        if (null == file) {
            download(File.createTempFile("empty-" + cid + "-", ".log"), resp);
            return;
        }
        File zfile = file;
        List<FileInfo> list = new ArrayList<>();
        for (File ff : new File(EnvUtils.getFlumeLogHomePath(cid)).listFiles()) {
            FileInfo fi = new FileInfo();
            fi.setName(ff.getName());
            fi.setPath(ff.getAbsolutePath());
            list.add(fi);
        }
        zfile = CmdWrapper.zip(list, cid + "-");
        download(cid + ".zip", zfile, resp);
    }

    protected List<MultipartBody> getMultipartBodys(MultipartFormDataInput multipartFormDataInput) {
        return multipartFormDataInput.getParts().stream().map(MultipartBody::new).collect(Collectors.toList());
    }

    protected void download(File file, HttpServletResponse resp) {
        try {
            download(file.getName(), FileUtils.openInputStream(file), resp);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    protected void download(String name, File file, HttpServletResponse resp) {
        try {
            download(name, FileUtils.openInputStream(file), resp);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    protected void download(String name, InputStream inputStream, HttpServletResponse resp)
            throws UnsupportedEncodingException {
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("content-type", "application/octet-stream");
        resp.setHeader("content-encoding", "gzip");
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(name, "UTF-8"));
        try (OutputStream out = new GZIPOutputStream(resp.getOutputStream()); InputStream in = inputStream) {
            IOUtils.copy(in, out);
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
