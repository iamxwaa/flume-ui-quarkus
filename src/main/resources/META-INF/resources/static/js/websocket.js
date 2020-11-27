layui.define(function (exports) {
    var obj = {
        log: function (url) {
            var ws = new WebSocket("wss://" + location.host + url);
            ws.onclose = function () {
                console.log("关闭日志通道");
            }
            var currentLines = 0;
            return {
                open: function (content_id, hello, maxLines, err_test, termOpt) {
                    var term = undefined != termOpt ? new Terminal(termOpt) : new Terminal();
                    var fitAddon = new FitAddon.FitAddon();
                    term.loadAddon(fitAddon);
                    term.open(document.getElementById(content_id));
                    fitAddon.fit();
                    var ok = false;
                    ws.onmessage = function (a) {
                        if (ok) {
                            if (-1 != maxLines && currentLines % maxLines == 0) {
                                term.clear();
                            }
                            if ((err_test).test(a.data)) {
                                // logContent.append("<p class='console-err'>" + a.data + "</p>");
                                term.writeln('\x1B[1;3;31m' + a.data + '\x1B[0m');
                            } else {
                                term.writeln(a.data);
                                // logContent.append("<p class='console-p'>" + a.data + "</p>");
                            }
                            if (-1 != maxLines) {
                                currentLines++;
                            }
                            return;
                        }
                        if (a.data == '200') {
                            console.log("建立日志通道成功");
                            ok = true;
                            if ("" != hello) {
                                ws.send(hello);
                            }
                        }
                    }
                }
                , close: function () {
                    ws.close();
                }
            }
        }
        , create: function (url) {
            var ws = new WebSocket("wss://" + location.host + url);
            ws.onclose = function () {
                console.log("关闭日志通道");
            }
            return ws;
        }
    }

    exports('ws', obj);
});