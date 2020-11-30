package org.github.toxrink.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.model.TemplateInfo;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.CommonUtils;
import org.github.toxrink.utils.EnvUtils;
import org.github.toxrink.utils.PageUtils;
import org.github.toxrink.utils.TemplateUtils;
import org.github.toxrink.utils.UploadUtils;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import x.os.CmdWrapper;
import x.os.FileInfo;
import x.utils.TimeUtils;

public class FileController {
    private static final Log LOG = LogFactory.getLog(FileController.class);

    // /**
    // * 文件上传
    // *
    // * @param mfile
    // * 上传文件
    // * @param request
    // * 上传请求
    // * @return 跳转页面
    // */
    // @POST
    // @Path("/file/upload")
    // @Produces(MediaType.APPLICATION_JSON)
    // public String upload(@QueryParam("inputfile") MultipartFile mfile,
    // HttpServletRequest request) {
    // File file = new File(UploadUtils.getStorePath(mfile.getOriginalFilename()));
    // boolean mark = true;
    // try {
    // FileOutputStream output = new FileOutputStream(file);
    // IOUtils.write(mfile.getBytes(), output);
    // IOUtils.closeQuietly(output);
    // LOG.info("upload file to " + file.getAbsolutePath());
    // } catch (IOException e) {
    // LOG.error("", e);
    // mark = false;
    // }
    // PageAlertUtils.writeInfo("文件上传" + (mark ? "成功" : "失败"));
    // return "redirect:/file";
    // }

    // /**
    // * jar包上传
    // *
    // * @param mfile
    // * 上传文件
    // * @param request
    // * 上传请求
    // * @return 跳转页面
    // */
    // @POST
    // @Path("/file/upload/jar")
    // @Produces(MediaType.APPLICATION_JSON)
    // public String uploadJar(@QueryParam MultipartFile mfile, HttpServletRequest
    // request) {
    // String dstFile = EnvUtils.getBaseHomePath() + "/flume/" +
    // UploadUtils.FLUME_JAR_DIR
    // + mfile.getOriginalFilename();
    // if (!mfile.getOriginalFilename().endsWith(".jar")) {
    // PageAlertUtils.writeInfo("文件上传失败,请上传jar文件");
    // return "redirect:/flume";
    // }
    // File file = new File(dstFile);
    // if (file.exists()) {
    // String dstFile2 = EnvUtils.getBaseHomePath() + "/flume/" +
    // UploadUtils.FLUME_JAR_DIR
    // + mfile.getOriginalFilename() + "." + TimeUtils.getTimestamp();
    // File dest = new File(dstFile2);
    // file.renameTo(dest);
    // LOG.info("目标文件已存在,备份为: " + dstFile2);
    // }
    // file = new File(dstFile);
    // boolean mark = true;
    // try {
    // FileOutputStream output = new FileOutputStream(file);
    // IOUtils.write(mfile.getBytes(), output);
    // IOUtils.closeQuietly(output);
    // LOG.info("upload file to " + file.getAbsolutePath());
    // } catch (IOException e) {
    // LOG.error("", e);
    // mark = false;
    // }
    // PageAlertUtils.writeInfo("文件上传" + (mark ? "成功" : "失败"));
    // return "redirect:/flume";
    // }

    /**
     * 删除历史文件
     * 
     * @param name
     *                 文件名称
     * @return 跳转页面
     */
    @GET
    @Path("/file/history/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteHistory(@QueryParam String name) {
        StringBuilder filePath = new StringBuilder(EnvUtils.getHistoryFilePath());
        filePath.append("/" + name);
        File file = new File(filePath.toString());
        boolean mark = true;
        if (file.exists()) {
            mark = file.delete();
            LOG.info("delete file " + file.getAbsolutePath() + " [" + mark + "]");
        }
        PageUtils.writeInfo("文件删除" + (mark ? "成功" : "失败"));
        return "redirect:/file";
    }

    /**
     * 删除文件
     *
     * @param name
     *                 文件名称
     * @return 跳转页面
     */
    @GET
    @Path("/file/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@QueryParam String name) {
        StringBuilder filePath = new StringBuilder(EnvUtils.getFilePath());
        filePath.append("/" + name);
        File file = new File(filePath.toString());
        boolean mark = true;
        if (file.exists()) {
            mark = file.delete();
            LOG.info("delete file " + file.getAbsolutePath() + " [" + mark + "]");
        }
        PageUtils.writeInfo("文件删除" + (mark ? "成功" : "失败"));
        return "redirect:/file";
    }

    /**
     * 删除jar
     * 
     * @param name
     *                 jar名称
     * @return 跳转页面
     */
    @GET
    @Path("/file/delete/jar")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteJar(@QueryParam String name) {
        String dstFile = EnvUtils.getBaseHomePath() + "/flume/" + UploadUtils.FLUME_JAR_DIR + name;
        File file = new File(dstFile);
        boolean mark = true;
        if (file.exists()) {
            mark = file.delete();
            LOG.info("delete file " + file.getAbsolutePath() + " [" + mark + "]");
        }
        PageUtils.writeInfo("文件删除" + (mark ? "成功" : "失败"));
        return "redirect:/flume";
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
    @Path("/file/save")
    @Produces(MediaType.APPLICATION_JSON)
    public String update(@QueryParam String path, @QueryParam String content) throws IOException {
        StringBuilder filePath = new StringBuilder(EnvUtils.getFilePath());
        filePath.append("/" + path);
        File file = new File(filePath.toString());

        boolean mark = false;
        if (file.exists()) {
            // 备份文件
            CommonUtils.backupFile(file, EnvUtils.getHistoryFilePath() + "/" + path);
            CommonUtils.writeFile(file, content);
            mark = true;
            LOG.info("update file " + file.getAbsolutePath());
        }
        PageUtils.writeInfo("文件修改" + (mark ? "成功" : "失败"));
        return "redirect:/file";
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
    @Path("/file/log4j/save")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateLog4j(@QueryParam String content) throws IOException {
        File file = new File(UploadUtils.getLog4j().getPath());
        boolean mark = false;
        if (file.exists()) {
            CommonUtils.writeFile(file, content);
            mark = true;
            LOG.info("update file " + file.getAbsolutePath());
        }
        PageUtils.writeInfo("文件修改" + (mark ? "成功" : "失败"));
        return "redirect:/flume#log4j";
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
    @Path("/log/download")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadLog(@QueryParam String cid, HttpServletResponse resp) throws IOException {
        File file = CollectUtils.getLogFileById(cid);
        if (null == file) {
            download(File.createTempFile("empty-" + cid + "-", ".log"), resp);
            return;
        }
        File zfile = file;
        ZipArchiveOutputStream zout = null;
        try {
            List<FileInfo> list = new ArrayList<>();
            for (File ff : new File(EnvUtils.getFlumeLogHomePath(cid)).listFiles()) {
                FileInfo fi = new FileInfo();
                fi.setName(ff.getName());
                fi.setPath(ff.getAbsolutePath());
                list.add(fi);
            }
            zfile = CmdWrapper.zip(list, cid + "-");
        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            IOUtils.closeQuietly(zout);
        }

        download(cid + ".zip", zfile, resp);
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
    @Path("/file/download")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadFile(@QueryParam String name, HttpServletResponse resp) {
        StringBuilder filePath = new StringBuilder(EnvUtils.getFilePath());
        filePath.append("/" + name);
        File file = new File(filePath.toString());
        LOG.info("download file " + file.getAbsolutePath());
        download(file, resp);
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
    @Path("/file/history/download")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadhistoryFile(@QueryParam String name, HttpServletResponse resp) {
        StringBuilder filePath = new StringBuilder(EnvUtils.getHistoryFilePath());
        filePath.append("/" + name);
        File file = new File(filePath.toString());
        LOG.info("download file " + file.getAbsolutePath());
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
    @Path("/file/download/jar")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadJarFile(@QueryParam String name, HttpServletResponse resp) {
        String dstFile = EnvUtils.getBaseHomePath() + "/flume/lib/" + name;
        File file = new File(dstFile);
        LOG.info("download file " + file.getAbsolutePath());
        download(file, resp);
    }

    /**
     * 模板打包下载
     * 
     * @param resp
     *                 返回请求
     */
    @GET
    @Path("/pkg/download")
    @Produces(MediaType.APPLICATION_JSON)
    public void pkgFile(HttpServletResponse resp) {
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
            LOG.error("", e);
        }
    }

    // /**
    // * 模板配置导入
    // *
    // * @param mfile
    // * 上传文件
    // * @param request
    // * 上传请求
    // * @return
    // */
    // @POST
    // @Path("/pkg/upload")
    // @Produces(MediaType.APPLICATION_JSON)
    // public String uploadPkg(@QueryParam("inputfile") MultipartFile mfile,
    // HttpServletRequest request) {
    // try {
    // byte[] buffer = new byte[1024];
    // int len = 0;
    // InputStream in = mfile.getInputStream();
    // ByteArrayOutputStream out = new ByteArrayOutputStream();
    // while ((len = in.read(buffer)) != -1) {
    // out.write(buffer, 0, len);
    // }
    // out.close();
    // byte[] tmp = out.toByteArray();
    // byte[] tmp2 = new byte[tmp.length];
    // for (int i = 0; i < tmp.length; i++) {
    // tmp2[tmp2.length - 1 - i] = (byte) (tmp[i] ^ 111);
    // }
    // ObjectInputStream oin = new ObjectInputStream(new
    // ByteArrayInputStream(tmp2));
    // @SuppressWarnings("unchecked")
    // List<TemplateInfo> list = (List<TemplateInfo>) oin.readObject();
    // list.forEach(f -> {
    // TemplateInfo ti = new TemplateInfo();
    // ti.setName(f.getName());
    // ti.setDesc(f.getDesc());
    // ti.setSetting(f.getSetting());
    // ti.setCreateTime(TimeUtils.format2(TimeUtils.getNow()));
    // ti.setUpdateTime(TimeUtils.format2(TimeUtils.getNow()));
    // try {
    // TemplateUtils.save(ti);
    // } catch (IOException e) {
    // LOG.error("", e);
    // }
    // });
    // PageAlertUtils.writeInfo("模板导入成功");
    // return "redirect:/template";
    // } catch (IOException | ClassNotFoundException e) {
    // LOG.error("", e);
    // }
    // return "/error";
    // }

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
    @Path("/collect/package")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadPackage(@QueryParam String cid, HttpServletResponse resp) throws IOException {
        List<FileInfo> list = CollectUtils.getCollectFilePath(cid);
        File zfile = CmdWrapper.zip(list, "flume-" + TimeUtils.getTimestamp() + "_");
        download(zfile, resp);
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
    @Path("/file/datafix/save")
    @Produces(MediaType.APPLICATION_JSON)
    public String saveDatafix(@QueryParam String datafixName, @QueryParam String datafixContent) {
        File file = new File(UploadUtils.getStorePath(datafixName));
        boolean mark = true;
        try {
            FileOutputStream output = new FileOutputStream(file);
            IOUtils.write(datafixContent.getBytes(), output);
            IOUtils.closeQuietly(output);
            LOG.info("upload file to " + file.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("", e);
            mark = false;
        }
        PageUtils.writeInfo("datafix文件生成" + (mark ? "成功" : "失败"));
        return "redirect:/file";
    }

    private void download(File file, HttpServletResponse resp) {
        try {
            download(file.getName(), FileUtils.openInputStream(file), resp);
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    private void download(String name, File file, HttpServletResponse resp) {
        try {
            download(name, FileUtils.openInputStream(file), resp);
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    private void download(String name, InputStream in, HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("content-type", "application/octet-stream");
        resp.setHeader("content-encoding", "gzip");
        resp.setContentType("application/octet-stream");
        try {
            resp.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(name, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            LOG.error("", e1);
        }
        OutputStream out = null;
        try {
            out = new GZIPOutputStream(resp.getOutputStream());
            IOUtils.copy(in, out);
        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
