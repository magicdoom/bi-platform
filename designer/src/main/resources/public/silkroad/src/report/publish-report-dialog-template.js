define(['template'], function (template) {
    function anonymous($data,$filename) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,url=$data.url,type=$data.type,$out='';$out+='<div class="con-releaseBox">\r\n    <div class="con-head">\r\n        <a href="';
        $out+=$escape(url);
        $out+='" target="_blank"><div class="con-hRead"></div></a>\r\n        ';
        if(type == 'POST'){
        $out+='\r\n        <a href="javascript:;"><div class="con-hReturn j-report-list"></div></a>\r\n        ';
        }else{
        $out+='\r\n        <a href="javascript:;"><div class="con-hEdit j-report-edit"></div></a>\r\n        ';
        }
        $out+='\r\n    </div>\r\n    <div class="con-body">\r\n        <div class="con-report-url">\r\n            <ul class="con-url">\r\n                <li>你可以将下面的报表地址分享给好友，或者通过邮件的方式发送出去</li>\r\n                <li id="copyUrlBtnCopyContent" class="con-url-text con-br">';
        $out+=$escape(url);
        $out+='</li>\r\n            </ul>\r\n            <ul class="con-url-a" id="copyUrlBtnContainer">\r\n                <li id="copyUrlBtn"><a><span class="conspan">复制URL地址</span></a></li>\r\n                <li><a href="';
        $out+=$escape(url);
        $out+='" target="_blank"><span class="conspan">新窗口浏览</span></a></li>\r\n            </ul>\r\n        </div>\r\n        <div class="con-tiled">\r\n            <ul class="con-report">\r\n                <li class="con-report-title">平铺式报表</li>\r\n                <li>将下面的代码加入到你的网页HTML代码中，让用户在你的网页上看到报表</li>\r\n                <li id="copyTiledBtnCopyContent" class="con-report-text con-br">\r\n                    &lt;iframe height="600" allowTransparency="true"\r\n                    style="width:100%;border:none;overflow:auto;"\r\n                    frameborder="0"\r\n                    src="';
        $out+=$escape(url);
        $out+='"&gt;&lt;/iframe&gt;\r\n                </li>\r\n            </ul>\r\n            <ul id="copyTiledBtnContainer" class="con-copy">\r\n                <li id="copyTiledBtn"><a href="javascript:;"><span class="conspan">复制代码</span></a></li>\r\n            </ul>\r\n        </div>\r\n        <div class="con-embedded">\r\n            <ul class="con-report">\r\n                <li class="con-report-title">嵌入式报表</li>\r\n                <li>下面的代码，能够适应各业务系统的个性化布局展现，可以实现报表的自适应调整</li>\r\n                <li id="copyEmbeddedBtnCopyContent" class="con-report-text con-br">\r\n                    &lt;iframe height="600" allowTransparency="true"\r\n                    style="width:100%;border:none;overflow:auto;" frameborder="0"\r\n                    src="';
        $out+=$escape(url);
        $out+='"&gt;&lt;/iframe&gt;\r\n                </li>\r\n            </ul>\r\n            <ul id="copyEmbeddedBtnContainer" class="con-copy">\r\n                <li id="copyEmbeddedBtn"><a href="javascript:;"><span class="conspan">复制代码</span></a></li>\r\n            </ul>\r\n        </div>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});