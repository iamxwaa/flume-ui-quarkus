// package org.github.toxrink.controller;

// import java.io.IOException;

// import javax.inject.Inject;
// import javax.servlet.http.HttpServletRequest;

// import com.google.gson.JsonSyntaxException;
// import org.github.toxrink.FlumeUi;
// import org.github.toxrink.config.EnvConfig;
// import org.github.toxrink.config.Menu;
// import org.github.toxrink.config.TouristConfig;
// import org.github.toxrink.filter.GlobalFilter;
// import org.github.toxrink.watcher.CollectorWatcher;
// import org.github.toxrink.watcher.FileWatcher;
// import org.jboss.resteasy.annotations.jaxrs.QueryParam;
// import org.github.toxrink.model.CollectInfo;
// import org.github.toxrink.model.JVMInfo;
// import org.github.toxrink.model.TemplateInfo;
// import org.github.toxrink.utils.CollectUtils;
// import org.github.toxrink.utils.CommonUtils;
// import org.github.toxrink.utils.ServerUtils;
// import org.github.toxrink.utils.TemplateUtils;
// import org.github.toxrink.utils.UploadUtils;

// import org.apache.commons.lang.StringUtils;
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;

// /**
//  * 页面跳转
//  * 
//  * @author xw
//  *
//  * @date 2018年8月2日
//  */
// public class PageController {
//     private static final Log LOG = LogFactory.getLog(PageController.class);

//     @Inject
//     private Menu menu;

//     @Inject
//     private EnvConfig envConfig;

//     /**
//      * 认证登录
//      * 
//      * @param model
//      *                  Model
//      * @param token
//      *                  认证参数
//      * @param req
//      *                  HttpServletRequest
//      * @return
//      */
//     @GetMapping({ "/login" })
//     public String login(@QueryParam String token, HttpServletRequest req) {
//         if (!envConfig.isAuth()) {
//             return "redirect:/index";
//         }
//         if (StringUtils.isEmpty(token)) {
//             return envConfig.isAuthLocal() ? menu.withVersion("login") : menu.withVersion("unauth");
//         }
//         boolean stat = false;
//         if (envConfig.isAuthLocal()) {
//             LOG.info("使用本地登录认证");
//             stat = token.equals(envConfig.getAuthToken());
//         } else {
//             LOG.info("使用远程登录认证");
//             String authUrl = envConfig.getAuthUrl();
//             if (StringUtils.isEmpty(authUrl) || StringUtils.isEmpty(token)) {
//                 LOG.error("验证链接或token为空");
//             }
//             stat = CommonUtils.checkAuthorization(token, authUrl);
//         }
//         LOG.info("登录认证结果: " + stat);
//         if (stat) {
//             req.getSession().setAttribute(GlobalFilter.SESSION_AUTH_TOKEN, token);
//             return "redirect:/index";
//         } else {
//             req.getSession().removeAttribute(GlobalFilter.SESSION_AUTH_TOKEN);
//         }
//         return menu.withVersion("unauth");
//     }

//     /**
//      * 登出
//      * 
//      * @param model
//      *                  Model
//      * @param req
//      *                  HttpServletRequest
//      * @return
//      */
//     @GetMapping({ "/logout" })
//     public String logout(Model model, HttpServletRequest req) {
//         req.getSession().removeAttribute(GlobalFilter.SESSION_AUTH_TOKEN);
//         return "redirect:/login";
//     }

//     /**
//      * 说明页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping({ "/readme" })
//     public String readme(Model model) {
//         return "readme";
//     }

//     /**
//      * 采集器状态页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping({ "/state" })
//     public String state(Model model) {
//         model.addAttribute("flumeinfo", ServerUtils.getFlumeInfoList());
//         return menu.withVersion("state");
//     }

//     /**
//      * 首页
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping({ "/index", "/" })
//     public String index(Model model) {
//         return menu.withVersion("index");
//     }

//     /**
//      * 概览页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/info")
//     public String info(Model model) {
//         model.addAttribute("totalCount", ServerUtils.getFlumeInfoList().size());
//         model.addAttribute("runCount", ServerUtils.getRunningFlumeInfoList().size());
//         model.addAttribute("restartCount", CollectUtils.getRestartCount());
//         model.addAttribute("jvm", new JVMInfo());
//         return menu.withVersion("info");
//     }

//     /**
//      * 模板页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/template")
//     public String temp(Model model) {
//         model.addAttribute("templateinfo", TemplateUtils.getTemplateInfoList());
//         return menu.withVersion("template");
//     }

//     /**
//      * 新建模板页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/template/new")
//     public String templateNew(Model model) {
//         return menu.withVersion("templateForm");
//     }

//     /**
//      * 更新模板页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/template/update")
//     public String templateUpdate(Model model, @QueryParam String tid) throws JsonSyntaxException, IOException {
//         String cp = TemplateUtils.getJsonFilePath(tid);
//         TemplateInfo ti = CommonUtils.readFileToObject(cp, TemplateInfo.class);
//         model.addAttribute("ti", ti);
//         return menu.withVersion("templateForm");
//     }

//     /**
//      * 采集器页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/collect")
//     public String collect(Model model) {
//         model.addAttribute("collectinfo", CollectUtils.getCollectInfoList());
//         return menu.withVersion("collect");
//     }

//     /**
//      * 新建采集器页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/collect/new")
//     public String collectNew(Model model) {
//         TouristConfig config = FlumeUi.getAppCtx().getBean(TouristConfig.class);
//         model.addAttribute("sources", config.getSourceMap());
//         model.addAttribute("channels", config.getChannelMap());
//         model.addAttribute("sinks", config.getSinkMap());
//         model.addAttribute("interceptors", config.getInterceptorMap());
//         return menu.withVersion("collectForm");
//     }

//     /**
//      * 更新采集器页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/collect/update")
//     public String collectUpdate(Model model, @QueryParam String cid) throws JsonSyntaxException, IOException {
//         String cp = CollectUtils.getJsonFilePath(cid);
//         CollectInfo ci = CommonUtils.readFileToObject(cp, CollectInfo.class);
//         model.addAttribute("ci", ci);
//         model.addAttribute("jsonConf", CommonUtils.getFileConfigToJson(CollectUtils.getConfPath(cid)));

//         TouristConfig config = FlumeUi.getAppCtx().getBean(TouristConfig.class);
//         model.addAttribute("sources", config.getSourceMap());
//         model.addAttribute("channels", config.getChannelMap());
//         model.addAttribute("sinks", config.getSinkMap());
//         model.addAttribute("interceptors", config.getInterceptorMap());

//         return menu.withVersion("collectForm");
//     }

//     /**
//      * 文件管理页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/file")
//     public String file(Model model) {
//         model.addAttribute("fileinfo", UploadUtils.getUploadInfoList());
//         return menu.withVersion("file");
//     }

//     /**
//      * 历史文件管理页面
//      *
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/file/history")
//     public String historyFile(Model model, @QueryParam String path) {
//         model.addAttribute("fileinfo", UploadUtils.getUploadInfoHistoryList(path));
//         return menu.withVersion("historyFile");
//     }

//     /**
//      * 文件修改页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/file/update")
//     public String fileUpdate(Model model, @QueryParam String name) throws IOException {
//         model.addAttribute("fi", UploadUtils.getFileInfoByPath(name));
//         return menu.withVersion("fileForm");
//     }

//     /**
//      * 历史文件查看页面
//      *
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/file/history/read")
//     public String fileHistory(Model model, @QueryParam String name) throws IOException {
//         model.addAttribute("fi", UploadUtils.getHistoryFileInfoByPath(name));
//         return menu.withVersion("historyFileForm");
//     }

//     /**
//      * 配置向导页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/tourist")
//     public String leader(Model model) {
//         TouristConfig config = FlumeUi.getAppCtx().getBean(TouristConfig.class);
//         model.addAttribute("sources", config.getSourceMap());
//         model.addAttribute("channels", config.getChannelMap());
//         model.addAttribute("sinks", config.getSinkMap());
//         model.addAttribute("interceptors", config.getInterceptorMap());
//         return menu.withVersion("tourist");
//     }

//     /**
//      * 配置向导页面2
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/tourist2")
//     public String leader2(Model model) {
//         TouristConfig config = FlumeUi.getAppCtx().getBean(TouristConfig.class);
//         model.addAttribute("sources", config.getSourceMap());
//         model.addAttribute("channels", config.getChannelMap());
//         model.addAttribute("sinks", config.getSinkMap());
//         model.addAttribute("interceptors", config.getInterceptorMap());
//         return menu.withVersion("tourist2");
//     }

//     /**
//      * 错误页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/error")
//     public String error(Model model) {
//         return menu.withVersion("error");
//     }

//     /**
//      * 未认证页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/unauth")
//     public String unauth(Model model) {
//         return menu.withVersion("unauth");
//     }

//     /**
//      * 测试页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/debug")
//     public String test(Model model) {
//         return menu.withVersion("debug");
//     }

//     /**
//      * js测试页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/test/js")
//     public String testJS(Model model) {
//         return menu.withVersion("js");
//     }

//     /**
//      * datafix测试页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/test/datafix")
//     public String testDataFix(Model model) {
//         return menu.withVersion("datafix");
//     }

//     /**
//      * 使用文件监控页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/filewatcher")
//     public String filewatcher(Model model) {
//         model.addAttribute("fileinfo", FileWatcher.getUseingFileList());
//         return menu.withVersion("filewatcher");
//     }

//     /**
//      * flume管理页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/flume")
//     public String flume(Model model) throws IOException {
//         model.addAttribute("fileinfo", UploadUtils.getFlumeJarList());
//         model.addAttribute("fi", UploadUtils.getLog4j());
//         return menu.withVersion("flume");
//     }

//     /**
//      * 更新页面
//      * 
//      * @param model
//      *                  update
//      * @return
//      */
//     @GetMapping("/update")
//     public String update(Model model) throws IOException {
//         return menu.withVersion("update");
//     }

//     /**
//      * datafix配置生成页面
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/setting/datafix")
//     public String settingDatafix(Model model) {
//         return menu.withVersion("datafixFile");
//     }

//     /**
//      * 采集器监控详情
//      * 
//      * @param model
//      *                  Model
//      * @return
//      */
//     @GetMapping("/collectorwatcher")
//     public String collectorwatcher(Model model) {
//         model.addAttribute("status", CollectorWatcher.getAutoRestartMap().values());
//         return menu.withVersion("collectorwatcher");
//     }

// }
