layui.define(['layer', 'element'], function (exports) {
    var obj = {
        alert: function (info) {
            if ("" == info) {
                return;
            }
            var layer = layui.layer;
            layer.open({
                type: 0
                , title: false
                , offset: 'auto'
                , closeBtn: 0
                , id: 'alert_box'
                , content: info
                , time: 5 * 1000
                , shade: 0.1
                , yes: function () {
                    layer.closeAll();
                }
            });
        }
        , setWaitShade: function (filter, cd) {
            if (undefined == cd) {
                cd = 61000;
            }
            $(filter).click(function () {
                layui.layer.load(0, { shade: 0.1, time: cd });
            })
        }
        , setDeleteConfirm: function (filter) {
            $(filter).click(function () {
                var url = $(this).attr("data");
                layui.use('layer', function () {
                    var layer = layui.layer;
                    layer.confirm('确定删除?', function (index) {
                        window.location.assign(url);
                        layer.close(index);
                    });
                });
            })
        }
        , closeLayer: function () {
            layui.layer.closeAll();
        }
        , setHeight: function (id, a, b) {
            if (undefined == b) {
                $("#" + id).height(a);
            } else {
                $("#" + id).height(a > b ? a : b);
            }
        }
    };

    exports('tools', obj);
});