// package org.github.toxrink.controller;

// import java.io.File;
// import java.io.IOException;
// import java.util.Optional;

// import org.apache.commons.io.FileUtils;
// import org.apache.commons.lang.StringUtils;
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import x.utils.TimeUtils;

// import com.google.gson.JsonSyntaxException;
// import org.github.toxrink.model.TemplateInfo;
// import org.github.toxrink.utils.PageAlertUtils;
// import org.github.toxrink.utils.TemplateUtils;

// @Controller
// public class TemplateController {
//     private static final Log LOG = LogFactory.getLog(TemplateController.class);

//     /**
//      * 保存模板
//      * 
//      * @param tid     模板id
//      * @param name    模板名称
//      * @param desc    模板描述
//      * @param setting flume配置
//      * @return 调整页面
//      * @throws JsonSyntaxException json格式错误
//      * @throws IOException         读写错误
//      */
//     @PostMapping("/template/save")
//     public String save(@RequestParam String tid, @RequestParam String name, @RequestParam String desc,
//             @RequestParam String setting) throws JsonSyntaxException, IOException {
//         if (StringUtils.isNoneEmpty(tid)) {
//             Optional<TemplateInfo> ti = TemplateUtils.getTemplateInfoById(tid);
//             if (ti.isPresent()) {
//                 ti.get().setName(name);
//                 ti.get().setDesc(desc);
//                 ti.get().setSetting(setting);
//                 ti.get().setUpdateTime(TimeUtils.format2(TimeUtils.getNow()));
//                 TemplateUtils.update(ti.get());
//                 PageAlertUtils.writeInfo("修改模板成功");
//             } else {
//                 LOG.error("does not exist template id " + tid);
//                 PageAlertUtils.writeInfo("修改模板ID: " + tid + " 不存在");
//             }
//         } else {
//             TemplateInfo ti = new TemplateInfo();
//             ti.setName(name);
//             ti.setDesc(desc);
//             ti.setSetting(setting);
//             ti.setCreateTime(TimeUtils.format2(TimeUtils.getNow()));
//             ti.setUpdateTime(TimeUtils.format2(TimeUtils.getNow()));
//             TemplateUtils.save(ti);
//             PageAlertUtils.writeInfo("新建模板成功");
//         }
//         return "redirect:/template";
//     }

//     /**
//      * 删除模板
//      * 
//      * @param tid 模板id
//      * @return 跳转页面
//      * @throws JsonSyntaxException json格式错误
//      * @throws IOException         读写错误
//      */
//     @GetMapping("/template/delete")
//     public String delete(@RequestParam String tid) throws JsonSyntaxException, IOException {
//         Optional<TemplateInfo> ti = TemplateUtils.getTemplateInfoById(tid);
//         if (ti.isPresent()) {
//             LOG.info("delete file " + ti.get().getJsonFilePath());
//             FileUtils.forceDelete(new File(ti.get().getJsonFilePath()));
//             PageAlertUtils.writeInfo("模板删除成功");
//             return "redirect:/template";
//         }
//         LOG.error("does not exist collect id " + tid);
//         PageAlertUtils.writeInfo("修改模板ID: " + tid + " 不存在");
//         return "redirect:/template";
//     }

// }
