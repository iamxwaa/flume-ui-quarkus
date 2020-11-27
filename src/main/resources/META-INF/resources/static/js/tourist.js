//各几点添加的个数
var SourceIndex = 1;
var ChannelIndex = 1;
var SinkIndex = 1;
var InterceptorIndex = 1;

// 数据节点信息
var PointDataList = []
// 数据节点连接信息
var PointLinkList = []

//当前表单提交的对象
//{ name: "es 入库", nameIndex: "k1", type: "k", pointDataIndex: 0, parameters: {} }
var CurrentObject = {}

//节点位置
var SourcePosition = { x: -80, y: 10 };
var ChannelPosition = { x: 0, y: 10 };
var SinkPosition = { x: 80, y: 10 };
var InterceptorPosition = { x: -120, y: 0 };

//连线点记录,保存的是CurrentObject对象
var PointA = {};
var PointB = {};

//配置文本
var ConfigText = "";

layui.use(['tools'], function () {
    layui.tools.setHeight("compent-list", window.screen.availHeight - 405);
    layui.tools.setHeight("t-container", window.screen.availHeight - 305);
    initTContainer();
});

/**
 * 弹出表单
 * @param {string} content 内容
 */
function showForm(content) {
    layui.use('layer', function () {
        var layer = layui.layer;
        layer.open({
            type: 1
            , area: ['800px', '600px']
            , id: CurrentObject.type
            , title: false
            , content:
                `
			<div class="layui-card">
					<div class="layui-card-header">`+ CurrentObject.name + `</div>
					<div class="layui-card-body" style="overflow: auto;height: 450px;">
						<div class="layui-form-item" id="tourist_body">
							`+ content + `
						</div>
					</div><br>
					<div class="layui-form-item layui-col-md-offset6">
						<div class="layui-input-block">
							<button onclick="addComponent()" class="layui-btn">确定</button>
							<button onclick="closeLayer()" class="layui-btn layui-btn-primary">关闭</button>
						</div>
					</div>
                </div>
			`
            , btnAlign: 'r'
            , closeBtn: 0
            , cancel: function () {
                layer.closeAll();
            }

        })
    });
}

/**
 * 请求并显示表单页面内容
 * @param {string} p 地址
 */
function renderForm(p) {
    $.get("/page/tourist/" + p + ".html", function (d) {
        showForm(d);
    });
}

function showSource(a) {
    CurrentObject.name = $(a).text().trim();
    CurrentObject.type = "r";
    renderForm("/source/" + $(a).attr("data"));
}
function showChannel(a) {
    CurrentObject.name = $(a).text().trim();
    CurrentObject.type = "c";
    renderForm("/channel/" + $(a).attr("data"));
}
function showSink(a) {
    CurrentObject.name = $(a).text().trim();
    CurrentObject.type = "k";
    renderForm("/sink/" + $(a).attr("data"));
}
function showInterceptor(a) {
    CurrentObject.name = $(a).text().trim();
    CurrentObject.type = "i";
    renderForm("/interceptor/" + $(a).attr("data"));
}

//添加节点到绘图中
function addComponent() {
    CurrentObject.parameters = {}
    $("#tourist_body input").each(function (a, b) {
        var id = $(b).attr("id");
        var value = $(b).val();
        if ("" != value) {
            CurrentObject.parameters[id] = value;
        }
    })
    switch (CurrentObject.type) {
        case "r":
            CurrentObject.nameIndex = "r" + SourceIndex++;
            CurrentObject.itemStyle = { color: '#FF5722' };
            CurrentObject.x = SourcePosition.x;
            CurrentObject.y = SourcePosition.y;
            SourcePosition.y += 20;
            break;
        case "c":
            CurrentObject.nameIndex = "c" + ChannelIndex++;
            CurrentObject.itemStyle = { color: '#5FB878' };
            CurrentObject.x = ChannelPosition.x;
            CurrentObject.y = ChannelPosition.y;
            ChannelPosition.y += 20;
            break;
        case "k":
            CurrentObject.nameIndex = "k" + SinkIndex++;
            CurrentObject.itemStyle = { color: '#01AAED' };
            CurrentObject.x = SinkPosition.x;
            CurrentObject.y = SinkPosition.y;
            SinkPosition.y += 20;
            break;
        case "i":
            CurrentObject.nameIndex = "i" + InterceptorIndex++;
            CurrentObject.itemStyle = { color: '#000000' };
            CurrentObject.x = InterceptorPosition.x;
            CurrentObject.y = InterceptorPosition.y;
            InterceptorPosition.y += 20;
            break;
    }

    //校验名称是否重复
    var num = 0
    for (var i = 0; i < PointDataList.length; i++) {
        if (("" + PointDataList[i].name).startsWith(CurrentObject.name)) {
            num++;
        }
    }
    if (0 != num) {
        CurrentObject.name += num;
    }
    CurrentObject.pointDataIndex = PointDataList.length;
    PointDataList.push(CurrentObject);
    CurrentObject = {};
    touristChart.setOption(newOption(), true);
    layui.layer.closeAll();
}

//添加连线记录
function putPointLink(link) {
    //重复性校验
    for (var i = 0; i < PointLinkList.length; i++) {
        if (PointLinkList[i].source == link.source && PointLinkList[i].target == link.target) {
            return;
        }
        if (PointLinkList[i].target == link.target && link.type == "k") {
            return;
        }
    }
    PointLinkList.push(link);
}

//查看预览
function privew() {
    //保存channel与source和sink的关联关系
    var channelLink = {};
    //保存source和interceptor的关联关系
    var sourceLink = {};

    PointLinkList.forEach(function (item) {
        if (item.type == "r") {
            if (item.targetObject.type == "c") {
                channel = channelLink[item.target];
                if (channel == undefined) {
                    channelLink[item.target] = { sources: [], sinks: [] };
                }
                channelLink[item.target].channel = item.targetObject;
                channelLink[item.target].sources.push(item);
            } else if (item.targetObject.type == "i") {
                source = sourceLink[item.name];
                if (source == undefined) {
                    sourceLink[item.name] = [];
                }
                sourceLink[item.name].push(item.targetObject);
            }
        } else if (item.type == "k") {
            channel = channelLink[item.source];
            if (channel == undefined) {
                channelLink[item.source] = { sources: [], sinks: [] };
            }
            channelLink[item.source].channel = item.sourceObject;
            channelLink[item.source].sinks.push(item);
        }
    })

    // console.log(channelLink);
    // console.log(sourceLink);
    //生成配置文本
    var text = "";
    text += "#agent配置信息";
    var sourceText = "\na1.sources = ";
    for (var item in channelLink) {
        channelLink[item].sources.forEach(function (i2) {
            if (-1 == sourceText.indexOf(i2.nameIndex)) {
                sourceText += (i2.nameIndex + " ");
            }
        })
    }
    var sinkText = "\na1.sinks = ";
    for (var item in channelLink) {
        channelLink[item].sinks.forEach(function (i2) {
            if (-1 == sinkText.indexOf(i2.nameIndex)) {
                sinkText += (i2.nameIndex + " ");
            }
        })
    }
    var channelText = "\na1.channels = ";
    for (var item in channelLink) {
        if (-1 == channelText.indexOf(channelLink[item].channel.nameIndex)) {
            channelText += (channelLink[item].channel.nameIndex + " ");
        }
    }
    text += channelText;
    text += sourceText;
    text += sinkText;

    //拼接参数
    var buildParameter = function (prefix, params) {
        var tmp = "";
        for (var i in params) {
            tmp += "\n";
            tmp += prefix;
            if (i == "G_TYPE") {
                tmp += ".type = ";
            } else {
                tmp += ("." + i + " = ");
            }
            tmp += params[i];
        }
        return tmp;
    }

    //保存添加过的组件
    var nameIndexInfo = {}
    //添加source对应的channel
    var sourceInfo = {};

    text += "\n\n#channel配置信息";
    for (var item in channelLink) {
        if (nameIndexInfo[channelLink[item].channel.nameIndex] == 1) {
            continue;
        }
        text += "\n#" + channelLink[item].channel.name;
        text += buildParameter("a1.channels." + channelLink[item].channel.nameIndex, channelLink[item].channel.parameters);
        nameIndexInfo[channelLink[item].channel.nameIndex] = 1;
    }

    text += "\n\n#source配置信息";
    for (var item in channelLink) {
        channelLink[item].sources.forEach(function (i2) {
            if (sourceInfo[i2.nameIndex] == undefined) {
                sourceInfo[i2.nameIndex] = [];
            }
            sourceInfo[i2.nameIndex].push(channelLink[item].channel.nameIndex);
            if (nameIndexInfo[i2.nameIndex] == 1) {
                return;
            }
            text += "\n#" + i2.name;
            text += buildParameter("a1.sources." + i2.nameIndex, i2.parameters);
            nameIndexInfo[i2.nameIndex] = 1;
            //添加interceptor信息
            var tmp1 = "\na1.sources." + i2.nameIndex + ".interceptors = "
            var tmp2 = "";
            if (undefined != sourceLink[i2.name]) {
                sourceLink[i2.name].forEach(function (it) {
                    tmp1 += (it.nameIndex + " ");
                    tmp2 += buildParameter("a1.sources." + i2.nameIndex + ".interceptors." + it.nameIndex, it.parameters);
                })
                text += tmp1;
                text += tmp2;
            }
        })
    }

    text += "\n\n#sink配置信息";
    for (var item in channelLink) {
        channelLink[item].sinks.forEach(function (i2) {
            if (nameIndexInfo[i2.nameIndex] == 1) {
                return;
            }
            text += "\n#" + i2.name;
            text += buildParameter("a1.sinks." + i2.nameIndex, i2.parameters);
            text += "\n#sink与channel配置信息";
            text += ("\na1.sinks." + i2.nameIndex + ".channel = " + channelLink[item].channel.nameIndex);
            nameIndexInfo[i2.nameIndex] = 1;
        })
    }

    text += "\n\n#source与channel配置信息";
    for (var i in sourceInfo) {
        text += ("\na1.sources." + i + ".channels = " + sourceInfo[i].join(" "));
    }

    console.log(text);
    showPrivewBox(text);
}

function showPrivewBox(text) {
    ConfigText = text;
    layui.use('layer', function () {
        var layer = layui.layer;
        layer.open({
            type: 1
            , area: ['800px', '600px']
            , id: "flume_config_form"
            , content:
                `
			<div class="layui-card">
					<div class="layui-card-header">Flume.conf
						<input type="text" id="tourist_title" placeholder="请输入标题" class="card-header-input">
					</div>
					<div class="layui-card-body" style="overflow: hiden;height: 450px;">
						<div class="layui-form-item layui-form-text">
							<pre class="card-body-pre" style="height: 450px;">
								<code id="codeBody" style="height: 450px;margin-top: -24px;" class="card-body-code properties">`+ ConfigText + `</code>
							</pre>
						</div>
					</div><br>
					<div class="layui-form-item layui-col-md-offset4">
						<div class="layui-input-block">
							<button onclick="saveAsTemplate()" class="layui-btn">保存为模板</button>
							<button onclick="saveAsCollector()" class="layui-btn">保存为采集器</button>
							<span style="padding: 0 10px 0 5px;">|</span>
							<button onclick="closeLayer()" class="layui-btn layui-btn-primary">关闭</button>
						</div>
					</div>
                </div>
			`
            , btnAlign: 'r'
            , title: false
            , closeBtn: 0
            , cancel: function () {
                layer.closeAll();
            }, success: function () {
                hljs.highlightBlock($("#codeBody").get()[0]);
            }

        })
    });
}

function saveAsCollector() {
    if (PointDataList.length == 0) {
        layer.msg("组件不能为空");
        return;
    }
    var desc = "组件清单: ";
    for (i in PointDataList) {
        desc += PointDataList[i].name;
        desc += ";";
    }
    var title = $("#tourist_title").val();
    if ("" == title) {
        title = "向导创建(" + getTime() + ")";
    }
    $.post("/collect/save", {
        "cid": "", "company": "", "product": "", "productVersion": "",
        "name": title, "desc": desc, "memSize": 2048, "setting": ConfigText
    }, function (d) {
        layer.confirm('创建完毕 !', function (index) {
            window.location.reload();
            layer.close(index);
        });
    })
}

function saveAsTemplate() {
    if (PointDataList.length == 0) {
        layer.msg("组件不能为空");
        return;
    }
    var desc = "组件清单: ";
    for (i in PointDataList) {
        desc += PointDataList[i].name;
        desc += ";";
    }
    var title = $("#tourist_title").val();
    if ("" == title) {
        title = "向导创建(" + getTime() + ")";
    }
    $.post("/template/save", { "tid": "", "name": title, "desc": desc, "setting": ConfigText }, function (d) {
        layer.confirm('创建完毕 !', function (index) {
            window.location.reload();
            layer.close(index);
        });
    })
}

//*************************echart部分************************************
var touristChart;
function initTContainer() {
    var dom = document.getElementById("t-container");
    touristChart = echarts.init(dom);
    touristChart.on('click', 'series', function (a) {
        if (a.dataType == "edge") {
            //删除连线
            PointLinkList = PointLinkList.filter(function (item) {
                return !(item.source == a.data.source && item.target == a.data.target);
            });
            touristChart.setOption(newOption(), true);
            return
        }
        if (a.dataType == "node") {
            if (PointA.type == undefined && PointB.type == undefined) {
                PointA = a.data;
                return;
            }
            if (PointA.type == undefined && PointB.type != undefined) {
                PointA = a.data;
            }
            if (PointA.type != undefined && PointB.type == undefined) {
                PointB = a.data;
            }

            //连线
            if (PointA.type == "r" && PointB.type == "c") {
                PointA.source = PointA.name;
                PointA.target = PointB.name;
                PointA.targetObject = PointB;
                putPointLink(PointA);
            } else if (PointA.type == "c" && PointB.type == "r") {
                PointB.source = PointB.name;
                PointB.target = PointA.name;
                PointB.targetObject = PointA;
                putPointLink(PointB);
            } else if (PointA.type == "c" && PointB.type == "k") {
                PointB.target = PointB.name;
                PointB.source = PointA.name;
                PointB.sourceObject = PointA;
                putPointLink(PointB);
            } else if (PointA.type == "k" && PointB.type == "c") {
                PointA.target = PointA.name;
                PointA.source = PointB.name;
                PointA.sourceObject = PointB;
                putPointLink(PointA);
            } else if (PointA.type == "r" && PointB.type == "i") {
                PointA.source = PointA.name;
                PointA.target = PointB.name;
                PointA.targetObject = PointB;
                PointA.lineStyle = { curveness: 0.2 };
                putPointLink(PointA);
                var pointA2 = {};
                pointA2.source = PointB.name;
                pointA2.target = PointA.name;
                pointA2.lineStyle = { curveness: 0.2 };
                pointA2.fake = true;
                putPointLink(pointA2);
            } else if (PointA.type == "i" && PointB.type == "r") {
                PointB.source = PointB.name;
                PointB.target = PointA.name;
                PointB.targetObject = PointA;
                PointB.lineStyle = { curveness: 0.2 };
                putPointLink(PointB);
                var pointB2 = {};
                pointB2.source = PointA.name;
                pointB2.target = PointB.name;
                pointB2.lineStyle = { curveness: 0.2 };
                pointB2.fake = true;
                putPointLink(pointB2);
            }
            touristChart.setOption(newOption(), true);
            PointA = {};
            PointB = {};
        }
    });
    touristChart.setOption(newOption(), true);
}

function newOption() {
    return {
        tooltip: {},
        animation: true,
        series: [
            {
                type: 'graph',
                layout: 'none',
                symbolSize: 40,
                symbol: "circle",
                roam: true,
                label: {
                    normal: {
                        show: true,
                        position: "bottom"
                    }
                },
                edgeSymbol: ['circle', 'arrow'],
                edgeSymbolSize: [4, 10],
                edgeLabel: {
                    normal: {
                        textStyle: {
                            fontSize: 20
                        }
                    }
                },
                data: PointDataList,
                links: PointLinkList,
                lineStyle: {
                    normal: {
                        opacity: 0.9,
                        width: 2,
                        curveness: 0
                    }
                }
            }
        ]
    };
}

function drag(ev) {
    ev.dataTransfer.setData("id", $(ev.target).parent().attr("id"));
}
function allowDrop(ev) {
    ev.preventDefault();
}
function drop(ev) {
    ev.preventDefault();
    var data = ev.dataTransfer.getData("id");
    $("#" + data).click();
}