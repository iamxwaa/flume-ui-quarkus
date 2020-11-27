// 模板引导
var SOURCE;
var CHANNEL;
var SINK;
var SETTING = "";
var TYPE = 0;

var sname = "-";
var cname = "-";
var ssname = "-";
var dataObjectList = [];
var linkObjectList = [];
var linkObject = { source: "", channel: "", sink: "" };

var Rnum = 1;
var Cnum = 1;
var Knum = 1;

var source_pos = { x: -80, y: 10 };
var channel_pos = { x: 0, y: 10 };
var sink_pos = { x: 80, y: 10 };
$(function () {
	initTContainer();
})
function saveAsCollector() {
	if (dataObjectList.length == 0) {
		layer.msg("组件不能为空");
		return;
	}
	var desc = "组件清单: ";
	for (i in dataObjectList) {
		desc += dataObjectList[i].name;
		desc += ";";
	}
	var title = $("#tourist_title").val();
	if ("" == title) {
		title = "向导创建(" + getTime() + ")";
	}
	$.post("/collect/save", {
		"cid": "", "company": "", "product": "", "productVersion": "",
		"name": title, "desc": desc, "memSize": 2048, "setting": SETTING
	}, function (d) {
		layer.confirm('创建完毕 !', function (index) {
			window.location.reload();
			layer.close(index);
		});
	})
}
function saveAsTemplate() {
	if (dataObjectList.length == 0) {
		layer.msg("组件不能为空");
		return;
	}
	var desc = "组件清单: ";
	for (i in dataObjectList) {
		desc += dataObjectList[i].name;
		desc += ";";
	}
	var title = $("#tourist_title").val();
	if ("" == title) {
		title = "向导创建(" + getTime() + ")";
	}
	$.post("/template/save", { "tid": "", "name": title, "desc": desc, "setting": SETTING }, function (d) {
		layer.confirm('创建完毕 !', function (index) {
			window.location.reload();
			layer.close(index);
		});
	})
}
function addComponent() {
	if (TYPE == 0) {
		SOURCE = {}
	} else if (TYPE == 1) {
		CHANNEL = {}
	} else if (TYPE == 2) {
		SINK = {}
	} else if (TYPE == 3) {
		return;
	}
	$("#tourist_body input").each(function (a, b) {
		var id = $(b).attr("id");
		var value = $(b).val();
		if ("" != value) {
			if (TYPE == 0) {
				SOURCE[id] = value;
			} else if (TYPE == 1) {
				CHANNEL[id] = value;
			} else if (TYPE == 2) {
				SINK[id] = value;
			}
		}
	})
	if (TYPE == 0) {
		SOURCE["_TYPE"] = 0;
		SOURCE["_RR"] = ("r" + Rnum++);
		putDataObjectList({ name: sname, value: SOURCE, x: source_pos.x, y: source_pos.y, itemStyle: { color: '#FF5722' } });
		source_pos.y += 20;
	} else if (TYPE == 1) {
		CHANNEL["_TYPE"] = 1;
		CHANNEL["_CC"] = ("c" + Cnum++);
		putDataObjectList({ name: cname, value: CHANNEL, x: channel_pos.x, y: channel_pos.y, itemStyle: { color: '#5FB878' } });
		channel_pos.y += 20;
	} else if (TYPE == 2) {
		SINK["_TYPE"] = 2;
		SINK["_KK"] = ("k" + Knum++);
		putDataObjectList({ name: ssname, value: SINK, x: sink_pos.x, y: sink_pos.y, itemStyle: { color: '#01AAED' } });
		sink_pos.y += 20;
	}
	opt = getOption(dataObjectList, linkObjectList);
	myChart.setOption(opt, true);
	layui.layer.closeAll();
}
function getPage(p, f) {
	$.get("/tourist/page?p=" + p, f);
}
function showForm(id, title, content) {
	layui.use('layer', function () {
		var layer = layui.layer;
		layer.open({
			type: 1
			, area: ['800px', '640px']
			, id: id
			, title: false
			, content:
				`
			<div class="layui-card">
					<div class="layui-card-header card-header">`+ title + `</div>
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
function showSource(a) {
	sname = $(a).text().trim();
	getPage("/source/" + $(a).attr("data"), function (d) {
		TYPE = 0;
		showForm("source0", sname, d);
	})
}
function showChannel(a) {
	cname = $(a).text().trim();
	getPage("/channel/" + $(a).attr("data"), function (d) {
		TYPE = 1;
		showForm("channel1", cname, d);
	})
}
function showSink(a) {
	ssname = $(a).text().trim();
	getPage("/sink/" + $(a).attr("data"), function (d) {
		TYPE = 2;
		showForm("sink2", ssname, d);
	})
}
function showInterceptor(a) {
	siname = $(a).text().trim();
	getPage("/interceptor/" + $(a).attr("data"), function (d) {
		TYPE = 4;
		showForm("interceptor4", siname, d);
	})
}
function privew() {
	var linklist = [];
	for (var i = 0; i < linkObjectList.length; i++) {
		for (var j = 0; j < linkObjectList.length; j++) {
			if (linkObjectList[i].target == linkObjectList[j].source) {
				linklist.push({
					source: linkObjectList[i].source,
					channel: linkObjectList[i].target,
					sink: linkObjectList[j].target
				})
			}
		}
	}
	var scslist = [];
	for (var i = 0; i < linklist.length; i++) {
		scs = {}
		for (var j = 0; j < dataObjectList.length; j++) {
			if (dataObjectList[j].name == linklist[i].source) {
				scs.source = dataObjectList[j].value;
			} else if (dataObjectList[j].name == linklist[i].channel) {
				scs.channel = dataObjectList[j].value;
			} else if (dataObjectList[j].name == linklist[i].sink) {
				scs.sink = dataObjectList[j].value;
			}
		}
		scslist.push(scs);
	}
	var rr = {};
	var cc = {};
	var kk = {};
	for (var i = 0; i < scslist.length; i++) {
		rr[scslist[i].source._RR] = 1;
		cc[scslist[i].channel._CC] = 1;
		kk[scslist[i].sink._KK] = 1;
	}
	TYPE = 3;
	var c = "#agent配置信息\n";
	c += "a1.sources = ";
	for (i in rr) {
		c += (i + " ");
	}
	c += "\na1.sinks = ";
	for (i in kk) {
		c += (i + " ");
	}
	c += "\na1.channels = ";
	for (i in cc) {
		c += (i + " ");
	}
	c += "\n";
	compent = {};
	for (var idx = 0; idx < scslist.length; idx++) {
		source = scslist[idx].source;
		if (compent[source._RR] == 1) {
			continue;
		}
		compent[source._RR] = 1;
		c += "\n#source配置信息\n";
		for (i in source) {
			if (i == "G_TYPE") {
				c += "a1.sources." + source._RR + ".type = " + source[i] + "\n";
				continue;
			}
			if (i == "_RR" || i == "_TYPE") {
				continue;
			}
			c += "a1.sources." + source._RR + "." + i + " = " + source[i] + "\n";
		}
	}
	for (var idx = 0; idx < scslist.length; idx++) {
		channel = scslist[idx].channel;
		if (compent[channel._CC] == 1) {
			continue;
		}
		compent[channel._CC] = 1;
		c += "\n#channel配置信息\n";
		for (i in channel) {
			if (i == "G_TYPE") {
				c += "a1.channels." + channel._CC + ".type = " + channel[i] + "\n";
				continue;
			}
			if (i == "_CC" || i == "_TYPE") {
				continue;
			}
			c += "a1.channels." + channel._CC + "." + i + " = " + channel[i] + "\n";
		}
	}
	for (var idx = 0; idx < scslist.length; idx++) {
		sink = scslist[idx].sink;
		if (compent[sink._KK] == 1) {
			continue;
		}
		compent[sink._KK] = 1;
		c += "\n#sink配置信息\n";
		for (i in sink) {
			if (i == "G_TYPE") {
				c += "a1.sinks." + sink._KK + ".type = " + sink[i] + "\n";
				continue;
			}
			if (i == "_KK" || i == "_TYPE") {
				continue;
			}
			c += "a1.sinks." + sink._KK + "." + i + " = " + sink[i] + "\n";
		}
		c += "a1.sinks." + sink._KK + ".channel = " + scslist[idx].channel._CC + "\n";
	}
	rsource = {};
	for (var idx = 0; idx < scslist.length; idx++) {
		source = scslist[idx].source;
		channel = scslist[idx].channel;
		if (rsource[source._RR] == undefined) {
			rsource[source._RR] = {};
		}
		rsource[source._RR][channel._CC] = 1;
	}
	for (i in rsource) {
		c += "\n#source 与 channel 配置信息\n";
		c += "a1.sources." + i + ".channels = ";
		for (j in rsource[i]) {
			c += (j + " ");
		}
		c += "\n";
	}
	SETTING = c;
	layui.use('layer', function () {
		var layer = layui.layer;
		layer.open({
			type: 1
			, area: ['800px', '640px']
			, id: "flume_config_form"
			, content:
				`
			<div class="layui-card">
					<div class="layui-card-header card-header">flume 配置
						<input type="text" id="tourist_title" placeholder="请输入标题" style="
						border: 1px solid #e6e6e6;
						border-radius: 2px;
						padding: 5px;
						width: 200px;
						margin-left: 10px;
					">
					</div>
					<div class="layui-card-body" style="overflow: hiden;height: 450px;">
						<div class="layui-form-item layui-form-text">
							<pre class="card-body-pre" style="height: 450px;">
								<code id="codeBody" style="height: 450px;margin-top: -24px;" class="card-body-code properties">`+ SETTING + `</code>
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

var myChart;
function initTContainer() {
	var dom = document.getElementById("t-container");
	myChart = echarts.init(dom);
	myChart.on('click', 'series', function (a) {
		if (a.dataType == "edge") {
			st = a.name.split(">")
			var newLinkList = []
			for (i in linkObjectList) {
				if (linkObjectList[i].source == st[0].trim() && linkObjectList[i].target == st[1].trim()) {
					continue
				}
				newLinkList.push(linkObjectList[i])
			}
			linkObjectList = newLinkList
			opt = getOption(dataObjectList, linkObjectList)
			myChart.setOption(opt, true)
			return
		}

		if (a.dataType == "node") {
			if (0 == a.value._TYPE) {
				linkObject.source = a.name
			} else if (1 == a.value._TYPE) {
				linkObject.channel = a.name
			} else if (2 == a.value._TYPE) {
				linkObject.sink = a.name
			}

			if (linkObject.source != "" && linkObject.channel != "") {
				putLinkObjectList({ source: linkObject.source, target: linkObject.channel })
				linkObject.source = ""
				linkObject.channel = ""
				opt = getOption(dataObjectList, linkObjectList)
				myChart.setOption(opt, true)
			} else if (linkObject.sink != "" && linkObject.channel != "") {
				for (i in linkObjectList) {
					if (linkObject.sink == linkObjectList[i].target) {
						return
					}
				}
				putLinkObjectList({ source: linkObject.channel, target: linkObject.sink })
				linkObject.sink = ""
				linkObject.channel = ""
				opt = getOption(dataObjectList, linkObjectList)
				myChart.setOption(opt, true)
			}
		}
	});
	myChart.setOption(getOption(), true)
}

function getOption(dataList, linkList) {
	return {
		tooltip: {},
		animation: false,
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
				data: dataList,
				links: linkList,
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
function putDataObjectList(d) {
	var num = 0
	for (var i = 0; i < dataObjectList.length; i++) {
		if (dataObjectList[i].name.startsWith(d.name)) {
			num++
		}
	}
	if (0 != num) {
		d.name += num
	}
	dataObjectList.push(d)
}
function putLinkObjectList(d) {
	m = false
	for (var i = 0; i < linkObjectList.length; i++) {
		if (linkObjectList[i].source == d.source && linkObjectList[i].target == d.target) {
			linkObjectList[i] = d
			m = true
			break
		}
	}
	if (!m) {
		linkObjectList.push(d)
	}
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