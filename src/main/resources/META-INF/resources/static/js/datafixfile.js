layui.use(['form', 'layedit', 'laytpl'], function () {
    var laytpl = layui.laytpl;
    var form = layui.form;
    form.on('submit(save)', function (data) {
        var settings = [];
        for (var i in data.field) {
            var key = i.split("#");
            if (settings[key[1]] == undefined) {
                settings[key[1]] = {};
            }
            if (key.length > 2) {
                settings[key[1]][key[0] + "#" + key[2]] = data.field[i];
            } else {
                settings[key[1]][key[0]] = data.field[i];
            }
        }

        var text = "conf.settings:";
        for (var i in settings) {
            if (i == undefined || i == "undefined") {
                continue;
            }
            text += "\n  - setting:"
            for (var j in settings[i]) {
                if (j.startsWith("type")) {
                    text += "\n      type: " + settings[i][j];
                } else if (j.startsWith("index")) {
                    text += "\n      index: " + settings[i][j];
                } else if (j.startsWith("time-key")) {
                    text += "\n      time-key: " + settings[i][j];
                } else if (j.startsWith("time-format")) {
                    text += "\n      time-format: " + settings[i][j];
                } else if (j.startsWith("es-time-format")) {
                    text += "\n      es-time-format: " + settings[i][j];
                } else if (j.startsWith("lv1-fields")) {
                    text += "\n      lv1-fields: " + settings[i][j];
                } else if (j.startsWith("lv2-fields")) {
                    if (undefined != settings[i][j] && "" != settings[i][j]) {
                        text += "\n      lv2-fields: " + settings[i][j];
                    }
                }
            }
            var textRename = "";
            var textAdd = "";
            var textFix = "";
            for (var j in settings[i]) {
                if (j.startsWith("renameOld")) {
                    var key = settings[i][j];
                    var value = "";
                    var r = j.split("#");
                    if (r.length > 1) {
                        value = settings[i]["renameNew#" + r[1]];
                    } else {
                        value = settings[i]["renameNew"];
                    }
                    if ("" == key || "" == value) {
                        continue;
                    }
                    textRename += "\n          - " + key + ":" + value;
                }
            }
            for (var j in settings[i]) {
                if (j.startsWith("addOld")) {
                    var key = settings[i][j];
                    var value = "";
                    var r = j.split("#");
                    if (r.length > 1) {
                        value = settings[i]["addNew#" + r[1]];
                    } else {
                        value = settings[i]["addNew"];
                    }
                    if ("" == key || "" == value) {
                        continue;
                    }
                    textAdd += "\n          - " + key + ":" + value;
                }
            }
            for (var j in settings[i]) {
                if (j.startsWith("fixName")) {
                    var key = settings[i][j];
                    var value = "";
                    var r = j.split("#");
                    if (r.length > 1) {
                        value = settings[i]["fixMethod#" + r[1]];
                    } else {
                        value = settings[i]["fixMethod"];
                    }
                    if ("" == key || "" == value) {
                        continue;
                    }
                    textFix += "\n          - " + key + ":" + value;
                }
            }
            var textTmp = "";
            if ("" != textRename) {
                textTmp += "\n        rename:";
                textTmp += textRename;
            }
            if ("" != textAdd) {
                textTmp += "\n        add:";
                textTmp += textAdd;
            }
            if ("" != textFix) {
                textTmp += "\n        handle:";
                textTmp += textFix;
            }
            if ("" != textTmp) {
                text += "\n      fix:";
                text += textTmp;
            }
        }
        form.val("settings", { datafixName: "datafix_auto_generate_" + getTimestamp() + ".yml", datafixContent: text });
    });
    var addCount = 1;
    var addAction = function () {
        $("button[name='copyRow']").click(function () {
            var p = $(this).parent().clone();
            p.children().remove("button[name='copyRow']");
            p.children().find("input").val("");
            p.insertAfter($(this).parent());
            p.children().find("input,select").each(function (a, b) {
                var n = $(b).attr("name");
                if (undefined != n) {
                    $(b).attr({ name: n + "#" + addCount });
                }
            })
            addCount++;
            form.render();
        })
    }

    var getTpl = settingTemplate.innerHTML
        , view = document.getElementById('settingTemplateView');
    var fields = {
        typeName: "type",
        indexName: "index",
        timeKeyName: "time-key",
        timeFormatName: "time-format",
        esTimeFormatName: "es-time-format",
        lv1FieldsName: "lv1-fields",
        lv2FieldsName: "lv2-fields",
        renameOldName: "renameOld",
        renameNewName: "renameNew",
        addOldName: "addOld",
        addNewName: "addNew",
        fixNameName: "fixName",
        fixMethodName: "fixMethod",
    }

    var settingFormList = [];
    var settingFormIndex = 0;
    var addSettingForm = function () {
        var fields2 = []
        for (var i in fields) {
            fields2[i] = fields[i] + "#" + settingFormIndex
        }
        settingFormList.push(fields2);
        laytpl(getTpl).render(settingFormList, function (html) {
            view.innerHTML = html;
            form.render();
            addAction();
            settingFormIndex++;
        })
    }

    $("#addSetting").click(function () {
        var data = form.val("settings");
        addSettingForm();
        form.val("settings", data);
    })
    addSettingForm();
})