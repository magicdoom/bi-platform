define(['template'], function (template) {
    function anonymous($data,$filename) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dataSourcesList=$data.dataSourcesList,$item=$data.$item,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<div>\r\n    <div class="con-set-cube c-f">\r\n        <div class="con-data-sources-list f-l j-root-data-sources-list">\r\n            <div class="title fs-14">请选择数据源</div>\r\n            ';
        $each(dataSourcesList,function($item,$index){
        $out+=' <span\r\n                class="btn-has-icon btn-has-icon-data-sources data-line c-p j-item';
        if($item.selected===true){
        $out+=' selected';
        }
        $out+='"\r\n                data-id="';
        $out+=$escape($item.id);
        $out+='">';
        $out+=$escape($item.name);
        $out+='</span>\r\n            ';
        });
        $out+='\r\n            ';
        if(dataSourcesList.length == 0){
        $out+='\r\n            <div class="empty-data ta-c">\r\n                暂无数据源\r\n                <a class="create-data-sources-link c-p td-u j-create-data-sources-link">\r\n                    现在去创建数据源\r\n                </a>\r\n            </div>\r\n            ';
        }
        $out+='\r\n        </div>\r\n        <div class="con-cube-list f-l j-con-cube-list">\r\n            <div class="title fs-14">请选择要实用的数据表（可多选）</div>\r\n            <div class="empty-data ta-c">暂无数据表</div>\r\n        </div>\r\n    </div>\r\n    <div class="form-common-line ta-c">\r\n        ';
        if(dataSourcesList.length > 0){
        $out+='<span\r\n            class="button button-flat-primary j-submit">提交</span>';
        }
        $out+=' <span\r\n            class="button button-flat-primary j-cancel">取消</span>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});