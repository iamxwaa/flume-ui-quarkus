layui.config({
    base: '/static/js/'
}).extend({
    tools: 'tools'
    , ws: 'websocket'
});

function closeLayer() {
    layui.use('layer', function () {
        var layer = layui.layer;
        layer.closeAll();
    });
}

function getTime() {
    a = new Date()
    y = a.getFullYear()
    m = a.getMonth() < 9 ? "0" + (a.getMonth() + 1) : (a.getMonth() + 1)
    d = a.getDate() < 10 ? "0" + a.getDate() : a.getDate()
    h = a.getHours() < 10 ? "0" + a.getHours() : a.getHours()
    mm = a.getMinutes() < 10 ? "0" + a.getMinutes() : a.getMinutes()
    s = a.getSeconds() < 10 ? "0" + a.getSeconds() : a.getSeconds()
    return y + "-" + m + "-" + d + " " + h + ":" + mm + ":" + s
}

function getTimestamp() {
    a = new Date()
    y = a.getFullYear()
    m = a.getMonth() < 9 ? "0" + (a.getMonth() + 1) : (a.getMonth() + 1)
    d = a.getDate() < 10 ? "0" + a.getDate() : a.getDate()
    h = a.getHours() < 10 ? "0" + a.getHours() : a.getHours()
    mm = a.getMinutes() < 10 ? "0" + a.getMinutes() : a.getMinutes()
    s = a.getSeconds() < 10 ? "0" + a.getSeconds() : a.getSeconds()
    return y + "" + m + "" + d + "" + h + "" + mm + "" + s
}