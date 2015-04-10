/**
 * Created by v_zhaoxiaoqiang on 2014/5/22.
 */
define(['backbone', 'dialog'], function (Backbone, dialog) {

    // 全局的log容错
    window.dataInsight = window.dataInsight || {};
    dataInsight.log = function (str) {
        if (typeof console == 'object' && typeof console.log == 'function') {
            console.log(str);
        }
    };

    // 全局ajax设置
    $.ajaxSetup({
        cache: false,
        dataType: 'json',
        dataFilter: function(response,dataType){

            var result;
            if(dataType == 'json' && window.JSON && window.JSON.parse){
                result = window.JSON.parse(response);
                // 后台返回数据失败
                if(result.status && result.status !== 0){
                    if(result.statusInfo === ''){
                        result.statusInfo = 'status为“' + result.status + '”的错误';
                    }
                    this.error(result.statusInfo);
                    // 抛出错误为了终止success的执行，以免扩大错误范围
                    throw Error(result.statusInfo);
                }
            }

            return response;
        },
        // 具体调用时配置了 error，会覆盖此error
        error: function(errorStr){

            if(typeof errorStr == 'object' && errorStr.status == '404'){
                dialog.error('404，请求的服务不存在！');
            }else{
                dialog.error(errorStr);
            }
        }
    });

    // 异步加载某模块，可在此处做一些路由处理
    var enter = function () {
        initProduct();

        $('#btnUpdate').click(function () {
            initProduct();
        });
    };

    var cmmp;
    function initProduct() {
        var vmJsonPath = '../test/demo/report/new-report/vm-json';
        require(['report/component-combination/enter', vmJsonPath], function(component, VmJson) {
            // var vmJsomObject = VmJson.handsonTable;
            //var vmJsomObject = VmJson.dateChart;
             var vmJsomObject = VmJson.treeChart;
            //var vmJsomObject = VmJson.twoTable;
            //var vmJsomObject = VmJson.liteolapTableChart;
            if (cmmp) {
                cmmp.dispose();
            } else {
                cmmp = component;
            }
            var options = {
                parentEl: $('.di-o_o-body')[0],
                rptHtml: vmJsomObject.html.join(''),
                rptJson: vmJsomObject.json,
                reportId: '66d1c58a02d7c0c0919f797f386d15df'
            };
            component.start(options);
        });
    }

    return {
        enter: enter
    };
});