define(['template'], function (template) {
    function anonymous($data,$filename) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<!-- 其他操作 -->\n<div class="data-format">\n    <div class="data-format-alone">\n        <div class="data-format-black">\n            <span>是否过滤空白行：</span>\n            <input type="checkbox"\n            ';
        if($data.dataFormat.value == "false" ){
        $out+='\n\n            ';
        }else{
        $out+='\n                checked="checked"\n            ';
        }
        $out+=' />\n        </div>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});