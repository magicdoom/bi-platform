define(['template'], function (template) {
    function anonymous($data,$filename) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<ul class="br-3 j-root">\r\n    <li class="data-sources-extend-title"><span>汇总方式</span></li>\r\n    <li class="br-3 data-sources-float-window-item j-method-type" data-value="SUM"><span>SUM</span></li>\r\n    <li class="br-3 data-sources-float-window-item j-method-type" data-value="COUNT"><span>COUNT</span></li>\r\n    <!--<li class="br-3 data-sources-float-window-item j-method-type" data-value="AVERAGE"><span>AVERAGE</span></li>-->\r\n    <li class="data-sources-extend-title"><span>其他功能项</span></li>\r\n    <li class="br-3 data-sources-float-window-item j-rename"><span>重命名</span></li>\r\n</ul>';
        return $out;
    }
    return { render: anonymous };
});